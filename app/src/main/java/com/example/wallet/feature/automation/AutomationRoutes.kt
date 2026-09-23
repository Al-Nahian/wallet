package com.example.wallet.feature.automation

/** [REVIEW_QUEUE]'s literal string is duplicated in `ProcessIncomingSmsUseCase`'s deep link
 * (domain code can't depend on the feature layer) — keep the two in sync, same pattern
 * `CheckBudgetAlertsUseCase`/`GenerateDueRecurringTransactionsUseCase` already use. */
object AutomationRoutes {
    const val SETTINGS = "automation/settings"
    const val REVIEW_QUEUE = "review-queue"
}
