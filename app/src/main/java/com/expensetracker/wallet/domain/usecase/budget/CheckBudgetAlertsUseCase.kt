package com.expensetracker.wallet.domain.usecase.budget

import com.expensetracker.wallet.core.common.formatMoney
import com.expensetracker.wallet.domain.model.NotificationType
import com.expensetracker.wallet.domain.repository.BudgetRepository
import com.expensetracker.wallet.domain.repository.NotificationRepository
import com.expensetracker.wallet.domain.repository.TransactionRepository
import com.expensetracker.wallet.domain.repository.TransactionSplitRepository
import com.expensetracker.wallet.domain.usecase.notification.CreateNotificationUseCase
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.first

/**
 * plan.md §17/§57/§86 — after any transaction change, re-checks every budget whose date range
 * covers [now] and fires a `BUDGET_WARNING` (>=80%) or `BUDGET_EXCEEDED` (>=100%) Notification
 * Center entry the first time it's crossed this period, via the Phase 4 choke point
 * `CreateNotificationUseCase` (never a standalone system notification). [BudgetRoutes.detail]'s
 * URL format ("budgets/<id>") is duplicated here as a literal since domain code can't depend on
 * the feature-layer route object — keep the two in sync.
 */
class CheckBudgetAlertsUseCase @Inject constructor(
    private val budgetRepository: BudgetRepository,
    private val transactionRepository: TransactionRepository,
    private val transactionSplitRepository: TransactionSplitRepository,
    private val notificationRepository: NotificationRepository,
    private val calculateBudgetUsage: CalculateBudgetUsageUseCase,
    private val evaluateBudgetAlert: EvaluateBudgetAlertUseCase,
    private val createNotification: CreateNotificationUseCase,
) {
    suspend operator fun invoke(now: Long = System.currentTimeMillis()) {
        val budgets = budgetRepository.observeBudgets().first().filter { now in it.startDate..it.endDate }
        if (budgets.isEmpty()) return

        val transactions = transactionRepository.observeTransactions().first()
        val splits = transactionSplitRepository.observeAllSplits().first()
        val notifications = notificationRepository.observeAll().first()
        val budgetCategoriesByBudget = budgetRepository.observeAllBudgetCategories().first().groupBy { it.budgetId }

        for (budget in budgets) {
            val categoryIds = budgetCategoriesByBudget[budget.id].orEmpty().map { it.categoryId }.toSet()
            val usage = calculateBudgetUsage(budget, categoryIds, transactions, splits)

            val alreadyNotified = notifications
                .filter {
                    it.relatedEntityId == budget.id &&
                        it.createdAt in budget.startDate..budget.endDate &&
                        (it.type == NotificationType.BUDGET_WARNING || it.type == NotificationType.BUDGET_EXCEEDED)
                }
                .map { it.type }
                .toSet()

            val alertType = evaluateBudgetAlert(usage.usagePercent, alreadyNotified) ?: continue

            val (title, body) = when (alertType) {
                NotificationType.BUDGET_EXCEEDED -> "Budget exceeded" to
                    "You've spent ${formatMoney(usage.spentMinor, budget.currency)} of your " +
                    "${formatMoney(usage.amountMinor, budget.currency)} \"${budget.name}\" budget."
                else -> "Budget warning" to
                    "You've used ${String.format(Locale.getDefault(), "%.0f", usage.usagePercent)}% of your " +
                    "\"${budget.name}\" budget (${formatMoney(usage.spentMinor, budget.currency)} of " +
                    "${formatMoney(usage.amountMinor, budget.currency)})."
            }

            createNotification(
                type = alertType,
                title = title,
                body = body,
                deepLink = "budgets/${budget.id}",
                relatedEntityType = "budget",
                relatedEntityId = budget.id,
            )
        }
    }
}
