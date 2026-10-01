package com.expensetracker.wallet.domain.usecase.budget

import com.expensetracker.wallet.domain.model.NotificationType
import javax.inject.Inject

/**
 * plan.md §17/§57 — decides whether a budget's current usage% warrants a new notification,
 * given which threshold types have already fired for this budget's current period. Decoupled
 * from the Android notification API and any repository so it's independently unit-testable
 * (the phase's own test plan asks for exactly this). [alreadyNotified] should only contain
 * notifications from the budget's *current* period — a new period always starts fresh.
 */
class EvaluateBudgetAlertUseCase @Inject constructor() {
    operator fun invoke(usagePercent: Double, alreadyNotified: Set<NotificationType>): NotificationType? {
        if (usagePercent >= 100.0 && NotificationType.BUDGET_EXCEEDED !in alreadyNotified) {
            return NotificationType.BUDGET_EXCEEDED
        }
        if (usagePercent >= 80.0 &&
            NotificationType.BUDGET_WARNING !in alreadyNotified &&
            NotificationType.BUDGET_EXCEEDED !in alreadyNotified
        ) {
            return NotificationType.BUDGET_WARNING
        }
        return null
    }
}
