package com.expensetracker.wallet.feature.budgets

import com.expensetracker.wallet.domain.model.Budget
import com.expensetracker.wallet.domain.usecase.budget.BudgetUsage

/** A budget paired with its current-period usage, as shown in the budgets list and the
 * dashboard's Monthly Budget section. */
data class BudgetSummary(
    val budget: Budget,
    val usage: BudgetUsage,
)
