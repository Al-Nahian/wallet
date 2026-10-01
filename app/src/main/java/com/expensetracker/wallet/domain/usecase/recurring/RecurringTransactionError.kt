package com.expensetracker.wallet.domain.usecase.recurring

/** User-facing recurring-rule validation errors (plan.md §56 — friendly messages, never a raw
 * exception, ever reach the UI). Thrown only as [RecurringTransactionValidationException]. */
sealed class RecurringTransactionError(val userMessage: String) {
    data object AccountRequired : RecurringTransactionError("Choose an account.")
    data object InvalidAmount : RecurringTransactionError("Enter a valid amount, e.g. 500.00.")
    data object InvalidType : RecurringTransactionError("A recurring rule can only be an expense or income, not a transfer.")
    data object InvalidDateRange : RecurringTransactionError("The end date must be after the next due date.")
    data object NotFound : RecurringTransactionError("We couldn't find this recurring item. It may have been removed.")
}

class RecurringTransactionValidationException(val error: RecurringTransactionError) : Exception(error.userMessage)
