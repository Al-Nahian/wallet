package com.example.wallet.feature.recurring

/** Sub-routes pushed on top of whichever top-level screen navigates here (Dashboard's "Upcoming
 * Recurring Payments" section, mainly) — each renders its own back-button Scaffold, same pattern
 * as `BudgetRoutes`/`AccountRoutes`. [LIST]'s "recurring" shape is duplicated as a literal in
 * `GenerateDueRecurringTransactionsUseCase`'s reminder deep link — keep the two in sync. */
object RecurringRoutes {
    const val RECURRING_ID_ARG = "recurringId"
    const val LIST = "recurring"
    const val CREATE = "recurring/create"
    const val EDIT_PATTERN = "recurring/{$RECURRING_ID_ARG}/edit"

    fun edit(recurringId: String) = "recurring/$recurringId/edit"
}
