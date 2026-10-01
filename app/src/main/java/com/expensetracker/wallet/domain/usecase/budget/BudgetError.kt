package com.expensetracker.wallet.domain.usecase.budget

/** User-facing budget validation errors (plan.md §56 — friendly messages, never a raw exception,
 * ever reach the UI). Thrown only as [BudgetValidationException]. */
sealed class BudgetError(val userMessage: String) {
    data object NameRequired : BudgetError("Please enter a budget name.")
    data object InvalidAmount : BudgetError("Enter a valid budget amount, e.g. 30000.00.")
    data object InvalidDateRange : BudgetError("The end date must be after the start date.")
    data object InvalidCategoryLimit : BudgetError("Enter a valid limit for each selected category.")
    data object BudgetNotFound : BudgetError("We couldn't find this budget. It may have been removed.")
}

class BudgetValidationException(val error: BudgetError) : Exception(error.userMessage)
