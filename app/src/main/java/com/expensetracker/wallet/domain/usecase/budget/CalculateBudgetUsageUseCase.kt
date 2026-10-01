package com.expensetracker.wallet.domain.usecase.budget

import com.expensetracker.wallet.domain.model.Budget
import com.expensetracker.wallet.domain.model.Transaction
import com.expensetracker.wallet.domain.model.TransactionSplit
import com.expensetracker.wallet.domain.model.TransactionType
import javax.inject.Inject

data class BudgetUsage(
    val budgetId: String,
    val amountMinor: Long,
    val spentMinor: Long,
    val remainingMinor: Long,
    val usagePercent: Double,
)

/**
 * plan.md §17/§25 `getBudgetUtilization()` — spent/remaining/usage% for a budget's date range.
 * [categoryIds] empty means an "overall" budget (every expense category counts); otherwise only
 * expenses attributed to one of [categoryIds] count. A split transaction's amount is attributed
 * to each split's own category, exactly like `GetCategorySpendUseCase` (Phase 8) — never to the
 * parent's, which is deliberately null for a split. Transfers/income never count (§26 rule 1),
 * since only `TransactionType.EXPENSE` rows are considered. Pure function over an already-loaded
 * fixture, independently unit-testable.
 */
class CalculateBudgetUsageUseCase @Inject constructor() {
    operator fun invoke(
        budget: Budget,
        categoryIds: Set<String>,
        transactions: List<Transaction>,
        splits: List<TransactionSplit>,
    ): BudgetUsage {
        val splitsByTransaction = splits.groupBy { it.transactionId }
        var spent = 0L

        for (transaction in transactions) {
            if (transaction.type != TransactionType.EXPENSE) continue
            if (transaction.date !in budget.startDate..budget.endDate) continue

            val transactionSplits = splitsByTransaction[transaction.id]
            if (transactionSplits.isNullOrEmpty()) {
                if (categoryIds.isEmpty() || transaction.categoryId in categoryIds) {
                    spent += transaction.amountMinor
                }
            } else {
                for (split in transactionSplits) {
                    if (categoryIds.isEmpty() || split.categoryId in categoryIds) {
                        spent += split.amountMinor
                    }
                }
            }
        }

        val remaining = budget.amountMinor - spent
        val usagePercent = if (budget.amountMinor <= 0L) 0.0 else spent * 100.0 / budget.amountMinor

        return BudgetUsage(
            budgetId = budget.id,
            amountMinor = budget.amountMinor,
            spentMinor = spent,
            remainingMinor = remaining,
            usagePercent = usagePercent,
        )
    }
}
