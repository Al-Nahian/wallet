package com.expensetracker.wallet.domain.usecase.recurring

import com.expensetracker.wallet.core.common.formatMoney
import com.expensetracker.wallet.core.common.newId
import com.expensetracker.wallet.domain.model.NotificationType
import com.expensetracker.wallet.domain.model.RecurringTransaction
import com.expensetracker.wallet.domain.model.Transaction
import com.expensetracker.wallet.domain.repository.AccountRepository
import com.expensetracker.wallet.domain.repository.RecurringTransactionRepository
import com.expensetracker.wallet.domain.repository.TransactionRepository
import com.expensetracker.wallet.domain.usecase.notification.CreateNotificationUseCase
import javax.inject.Inject

/**
 * plans/11-recurring-goals.md — the recurring engine, run daily by `RecurringTransactionWorker`.
 * Never called from a Composable/ViewModel directly (plan.md §69 rule 19: automation lives in the
 * domain layer, not the UI). For every active rule whose `nextDate` has arrived:
 *  - [RecurringTransaction.autoPost] true: posts a real [Transaction] immediately, tagged with
 *    this rule's id via `recurringTransactionId` for traceability, then fires an informational
 *    `RECURRING_DUE` notification ("X was recorded automatically").
 *  - false: fires a `RECURRING_DUE` reminder only ("X is due today") and leaves the user to
 *    record it manually from the Recurring list.
 * Either way `nextDate` advances exactly once per call for a given rule (via [RecurringSchedule]),
 * so a reminder-only rule pings once per occurrence, not once per day it stays unrecorded. A rule
 * whose advanced `nextDate` would land after its `endDate` is deactivated instead of firing
 * forever — matching plan.md §69's "no runaway automation" spirit.
 *
 * [RecurringRoutes][com.expensetracker.wallet.feature.recurring.RecurringRoutes]'s "recurring" URL and
 * [com.expensetracker.wallet.feature.transactions.TransactionRoutes]'s "transactions/<id>/edit" shape
 * are duplicated here as literals since domain code can't depend on the feature layer — keep the
 * three in sync (same pattern `CheckBudgetAlertsUseCase` already uses for budget deep links).
 */
class GenerateDueRecurringTransactionsUseCase @Inject constructor(
    private val recurringTransactionRepository: RecurringTransactionRepository,
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository,
    private val createNotification: CreateNotificationUseCase,
) {
    suspend operator fun invoke(now: Long = System.currentTimeMillis()) {
        val due = recurringTransactionRepository.getDue(now)
        for (rule in due) {
            if (rule.autoPost) {
                postTransaction(rule, now)
            } else {
                remind(rule)
            }
            advance(rule)
        }
    }

    private suspend fun postTransaction(rule: RecurringTransaction, now: Long) {
        val account = accountRepository.getAccount(rule.accountId) ?: return
        val transaction = Transaction(
            id = newId(),
            accountId = rule.accountId,
            type = rule.type,
            amountMinor = rule.amountMinor,
            currency = rule.currency,
            categoryId = rule.categoryId,
            payee = rule.payee,
            note = rule.note,
            date = rule.nextDate,
            createdAt = now,
            updatedAt = now,
            isRecurring = true,
            deletedAt = null,
            transferId = null,
            recurringTransactionId = rule.id,
        )
        transactionRepository.create(transaction)
        createNotification(
            type = NotificationType.RECURRING_DUE,
            title = "${displayName(rule)} recorded",
            body = "${formatMoney(rule.amountMinor, rule.currency)} was automatically recorded to ${account.name}.",
            deepLink = "transactions/${transaction.id}/edit",
            relatedEntityType = "recurring_transaction",
            relatedEntityId = rule.id,
        )
    }

    private suspend fun remind(rule: RecurringTransaction) {
        createNotification(
            type = NotificationType.RECURRING_DUE,
            title = "${displayName(rule)} is due",
            body = "${formatMoney(rule.amountMinor, rule.currency)} is due today. Tap to record it.",
            deepLink = "recurring",
            relatedEntityType = "recurring_transaction",
            relatedEntityId = rule.id,
        )
    }

    private suspend fun advance(rule: RecurringTransaction) {
        val nextDate = RecurringSchedule.nextOccurrence(rule.nextDate, rule.frequency)
        val stillActive = rule.endDate == null || nextDate <= rule.endDate
        recurringTransactionRepository.update(rule.copy(nextDate = nextDate, isActive = stillActive))
    }

    private fun displayName(rule: RecurringTransaction): String = rule.payee ?: "Recurring payment"
}
