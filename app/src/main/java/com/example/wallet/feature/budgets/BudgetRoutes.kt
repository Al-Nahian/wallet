package com.example.wallet.feature.budgets

/** Sub-routes pushed on top of whichever top-level screen navigates here (Dashboard's Monthly
 * Budget card, mainly) — each renders its own back-button Scaffold, same pattern as
 * `AccountRoutes`. [DETAIL_PATTERN]'s "budgets/<id>" shape is duplicated as a literal in
 * `CheckBudgetAlertsUseCase`'s deep links — keep the two in sync. */
object BudgetRoutes {
    const val BUDGET_ID_ARG = "budgetId"
    const val LIST = "budgets"
    const val CREATE = "budgets/create"
    const val DETAIL_PATTERN = "budgets/{$BUDGET_ID_ARG}"
    const val EDIT_PATTERN = "budgets/{$BUDGET_ID_ARG}/edit"

    fun detail(budgetId: String) = "budgets/$budgetId"
    fun edit(budgetId: String) = "budgets/$budgetId/edit"
}
