package com.example.wallet.domain.usecase.dashboard

import com.example.wallet.domain.model.Category
import com.example.wallet.domain.model.Transaction
import com.example.wallet.domain.model.TransactionSplit
import com.example.wallet.domain.model.TransactionType
import javax.inject.Inject

data class CategorySpend(
    val categoryId: String?,
    val categoryName: String,
    val amountMinor: Long,
    val percentage: Double,
)

/**
 * plan.md §20/§25 — "Where did I spend it?" A split transaction's amount is attributed to each
 * split's own category (never to the parent's, which is deliberately null for a split — Phase
 * 6/7), so the breakdown always reflects real category spend rather than lumping split purchases
 * under "Uncategorized". Pure function operating on an already-loaded fixture, independently
 * unit-testable per plan.md's test plan.
 */
class GetCategorySpendUseCase @Inject constructor() {
    operator fun invoke(
        transactions: List<Transaction>,
        splits: List<TransactionSplit>,
        categoriesById: Map<String, Category>,
    ): List<CategorySpend> {
        val splitsByTransaction = splits.groupBy { it.transactionId }
        val totals = LinkedHashMap<String?, Long>()

        for (transaction in transactions) {
            if (transaction.type != TransactionType.EXPENSE) continue

            val transactionSplits = splitsByTransaction[transaction.id]
            if (transactionSplits.isNullOrEmpty()) {
                totals[transaction.categoryId] = (totals[transaction.categoryId] ?: 0L) + transaction.amountMinor
            } else {
                for (split in transactionSplits) {
                    totals[split.categoryId] = (totals[split.categoryId] ?: 0L) + split.amountMinor
                }
            }
        }

        val grandTotal = totals.values.sum()
        return totals.entries
            .sortedByDescending { it.value }
            .map { (categoryId, amountMinor) ->
                CategorySpend(
                    categoryId = categoryId,
                    categoryName = categoryId?.let { categoriesById[it]?.name } ?: "Uncategorized",
                    amountMinor = amountMinor,
                    percentage = if (grandTotal == 0L) 0.0 else amountMinor * 100.0 / grandTotal,
                )
            }
    }
}
