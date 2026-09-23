package com.example.wallet.domain.usecase.importexport

import com.example.wallet.core.common.newId
import com.example.wallet.domain.model.Account
import com.example.wallet.domain.model.AccountType
import com.example.wallet.domain.model.Category
import com.example.wallet.domain.model.Label
import com.example.wallet.domain.model.Transaction
import com.example.wallet.domain.repository.AccountRepository
import com.example.wallet.domain.repository.CategoryRepository
import com.example.wallet.domain.repository.LabelRepository
import com.example.wallet.domain.repository.TransactionRepository
import java.util.Locale
import javax.inject.Inject

/**
 * Commits the accepted, error-free rows from the import preview (plans/12-import-export.md).
 * Missing accounts referenced by name are created as `OTHER`/row currency before the batch
 * insert; unmatched categories stay uncategorized rather than auto-creating category hierarchy
 * (a deliberate scope decision — see [MapCsvRowsUseCase]'s doc comment), and a row's label name
 * is found-or-created the same way accounts are. The transaction rows themselves land in the
 * database as a single atomic batch via [TransactionRepository.createBatch], so a
 * cancelled/failed import leaves the database unchanged (§36's atomicity acceptance criterion);
 * label assignment happens as a follow-up step per row, same as everywhere else in the app that
 * isn't a single Room `@Transaction` (e.g. `AssignCategoryUseCase`).
 */
class ImportTransactionsUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository,
    private val categoryRepository: CategoryRepository,
    private val labelRepository: LabelRepository,
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
            val categoryId = row.categoryName?.let { categoryIdByLowerName[it.trim().lowercase(Locale.US)] }

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
        return transactions.size
    }
}
