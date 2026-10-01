package com.expensetracker.wallet.domain.usecase.importexport

import com.expensetracker.wallet.core.common.newId
import com.expensetracker.wallet.domain.model.Account
import com.expensetracker.wallet.domain.model.Category
import com.expensetracker.wallet.domain.model.Label
import com.expensetracker.wallet.domain.model.Template
import com.expensetracker.wallet.domain.repository.TemplateRepository
import java.util.Locale
import javax.inject.Inject

/** Commits the accepted, error-free, non-duplicate rows from [parseTemplateRows]. Account,
 * category, and label are resolved by name here rather than in the parser, same split as every
 * other Import*UseCase in this package. */
class ImportTemplatesUseCase @Inject constructor(
    private val templateRepository: TemplateRepository,
) {
    suspend operator fun invoke(
        rows: List<ParsedTemplateRow>,
        existingAccounts: List<Account>,
        existingCategories: List<Category>,
        existingLabels: List<Label>,
    ): Int {
        val committable = rows.filter { it.accepted && it.errors.isEmpty() }
        if (committable.isEmpty()) return 0

        val accountIdByLowerName = existingAccounts.associateBy { it.name.trim().lowercase(Locale.US) }
        val categoryIdByLowerName = existingCategories.associateBy { it.name.trim().lowercase(Locale.US) }
        val labelIdByLowerName = existingLabels.associateBy { it.name.trim().lowercase(Locale.US) }
        val now = System.currentTimeMillis()

        var committedCount = 0
        committable.forEach { row ->
            val accountId = accountIdByLowerName[row.accountName!!.lowercase(Locale.US)]?.id ?: return@forEach
            val categoryId = categoryIdByLowerName[row.categoryName!!.lowercase(Locale.US)]?.id ?: return@forEach
            val labelId = labelIdByLowerName[row.labelName!!.lowercase(Locale.US)]?.id ?: return@forEach

            templateRepository.create(
                Template(
                    id = newId(),
                    name = row.name!!.trim(),
                    accountId = accountId,
                    categoryId = categoryId,
                    labelId = labelId,
                    payee = row.payee,
                    place = row.place,
                    createdAt = now,
                ),
            )
            committedCount++
        }
        return committedCount
    }
}
