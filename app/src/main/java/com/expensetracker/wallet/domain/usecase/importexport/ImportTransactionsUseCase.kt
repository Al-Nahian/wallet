package com.expensetracker.wallet.domain.usecase.importexport

import com.expensetracker.wallet.core.common.newId
import com.expensetracker.wallet.domain.model.Account
import com.expensetracker.wallet.domain.model.AccountType
import com.expensetracker.wallet.domain.model.Category
import com.expensetracker.wallet.domain.model.Label
import com.expensetracker.wallet.domain.model.NotificationType
import com.expensetracker.wallet.domain.model.Transaction
import com.expensetracker.wallet.domain.repository.AccountRepository
import com.expensetracker.wallet.domain.repository.CategoryRepository
import com.expensetracker.wallet.domain.repository.LabelRepository
import com.expensetracker.wallet.domain.repository.TransactionRepository
import com.expensetracker.wallet.domain.usecase.notification.CreateNotificationUseCase
import java.util.Locale
import javax.inject.Inject

/**
 * Commits the accepted, error-free rows from the import preview (plans/12-import-export.md).
 * Missing accounts referenced by name are created as `OTHER`/row currency before the batch
 * insert; unmatched categories stay uncategorized rather than auto-creating category hierarchy
 * (a deliberate scope decision — see [MapCsvRowsUseCase]'s doc comment) and instead raise a
 * [NotificationType.TRANSACTION_NEEDS_REVIEW] notification so the user reconciles them to the
 * closest existing category by hand; a row's label name, by contrast, is found-or-created the
 * same way accounts are, since a label carries no taxonomy to get wrong (plan.md §69 rule 8 is a
 * categories-only constraint). The transaction rows themselves land in the database as a single
 * atomic batch via [TransactionRepository.createBatch], so a cancelled/failed import leaves the
 * database unchanged (§36's atomicity acceptance criterion); label assignment happens as a
 * follow-up step per row, same as everywhere else in the app that isn't a single Room
 * `@Transaction` (e.g. `AssignCategoryUseCase`).
 */
class ImportTransactionsUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository,
    private val categoryRepository: CategoryRepository,
    private val labelRepository: LabelRepository,
    private val createNotification: CreateNotificationUseCase,
) {
    suspend operator fun invoke(
        rows: List<ParsedImportRow>,
        existingAccounts: List<Account>,
        existingCategories: List<Category>,
        existingLabels: List<Label>,
    ): Int {
        val committable = rows.filter { it.accepted && it.errors.isEmpty() }
        if (committable.isEmpty()) return 0

        val accountIdByLowerName = existingAccounts
            .associateBy { it.name.trim().lowercase(Locale.US) }
            .mapValues { (_, account) -> account.id }
            .toMutableMap()
        val categoryIdByLowerName = existingCategories
            .associateBy { it.name.trim().lowercase(Locale.US) }
            .mapValues { (_, category) -> category.id }
        val labelIdByLowerName = existingLabels
            .associateBy { it.name.trim().lowercase(Locale.US) }
            .mapValues { (_, label) -> label.id }
            .toMutableMap()

        val now = System.currentTimeMillis()
        val labelAssignments = mutableListOf<Pair<String, String>>()
        var unmatchedCategoryCount = 0

        val transactions = committable.map { row ->
            val accountKey = row.accountName!!.trim().lowercase(Locale.US)
            val accountId = accountIdByLowerName.getOrPut(accountKey) {
                val created = Account(
                    id = newId(),
                    name = row.accountName.trim(),
                    type = AccountType.OTHER,
                    institutionId = null,
                    currency = row.currency ?: "BDT",
                    openingBalanceMinor = 0L,
                    isArchived = false,
                    createdAt = now,
                    updatedAt = now,
                )
                accountRepository.create(created)
                created.id
            }
            val requestedCategoryName = row.categoryName?.trim()?.takeIf { it.isNotEmpty() }
            val categoryId = requestedCategoryName?.let { categoryIdByLowerName[it.lowercase(Locale.US)] }
            if (requestedCategoryName != null && categoryId == null) {
                unmatchedCategoryCount++
            }

            val transactionId = newId()
            row.labelName?.trim()?.takeIf { it.isNotEmpty() }?.let { labelName ->
                val labelId = labelIdByLowerName.getOrPut(labelName.lowercase(Locale.US)) {
                    val created = Label(id = newId(), name = labelName, color = "#9E9E9E", createdAt = now)
                    labelRepository.create(created)
                    created.id
                }
                labelAssignments += transactionId to labelId
            }

            Transaction(
                id = transactionId,
                accountId = accountId,
                type = row.type!!,
                amountMinor = row.amountMinor!!,
                currency = row.currency ?: "BDT",
                categoryId = categoryId,
                payee = row.payee,
                note = row.note,
                date = row.dateMillis!!,
                createdAt = now,
                updatedAt = now,
                isRecurring = false,
                deletedAt = null,
            )
        }

        transactionRepository.createBatch(transactions)
        labelAssignments.forEach { (transactionId, labelId) -> labelRepository.assign(transactionId, labelId) }

        if (unmatchedCategoryCount > 0) {
            val plural = unmatchedCategoryCount > 1
            createNotification(
                type = NotificationType.TRANSACTION_NEEDS_REVIEW,
                title = "Reconcile ${unmatchedCategoryCount} imported ${if (plural) "categories" else "category"}",
                body = if (plural) {
                    "$unmatchedCategoryCount imported transactions used a category name we don't recognize. Tap to assign the closest match yourself."
                } else {
                    "One imported transaction used a category name we don't recognize. Tap to assign the closest match yourself."
                },
                deepLink = "transactions",
            )
        }

        return transactions.size
    }
}
