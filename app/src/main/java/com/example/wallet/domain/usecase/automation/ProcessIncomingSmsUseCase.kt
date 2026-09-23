package com.example.wallet.domain.usecase.automation

import com.example.wallet.core.automation.AutomationSettingsRepository
import com.example.wallet.core.common.formatMoney
import com.example.wallet.core.common.minorUnitsToEditableString
import com.example.wallet.core.common.newId
import com.example.wallet.domain.model.AutomationCandidate
import com.example.wallet.domain.model.AutomationCandidateStatus
import com.example.wallet.domain.model.NotificationType
import com.example.wallet.domain.model.ParseConfidence
import com.example.wallet.domain.model.TransactionSource
import com.example.wallet.domain.model.TransactionType
import com.example.wallet.domain.repository.AccountRepository
import com.example.wallet.domain.repository.AutomationCandidateRepository
import com.example.wallet.domain.repository.TransactionRepository
import com.example.wallet.domain.usecase.automation.parser.ParsedTransaction
import com.example.wallet.domain.usecase.automation.parser.SmsParserRegistry
import com.example.wallet.domain.usecase.budget.CheckBudgetAlertsUseCase
import com.example.wallet.domain.usecase.notification.CreateNotificationUseCase
import com.example.wallet.domain.usecase.transaction.CreateTransactionUseCase
import com.example.wallet.domain.usecase.transaction.CreateTransferUseCase
import javax.inject.Inject
import kotlinx.coroutines.flow.first

/**
 * plan.md §87 — the SMS capture pipeline's single entry point, called by `SmsReceiver` for every
 * incoming message. Parse -> dedup -> resolve accounts -> route to auto-add or the Review Queue,
 * always ending in a Notification Center row either way (§86) so the user finds out either way,
 * never silently. Fails closed at every step per §68: an unparseable message, a disabled setting,
 * or a duplicate all simply return without creating anything or throwing.
 */
class ProcessIncomingSmsUseCase @Inject constructor(
    private val parserRegistry: SmsParserRegistry,
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository,
    private val automationCandidateRepository: AutomationCandidateRepository,
    private val automationSettingsRepository: AutomationSettingsRepository,
    private val resolveAccountHint: ResolveAccountHintUseCase,
    private val categorizeMerchant: CategorizeMerchantUseCase,
    private val createTransactionUseCase: CreateTransactionUseCase,
    private val createTransferUseCase: CreateTransferUseCase,
    private val checkBudgetAlertsUseCase: CheckBudgetAlertsUseCase,
    private val createNotification: CreateNotificationUseCase,
) {
    suspend operator fun invoke(sender: String, body: String, receivedAt: Long) {
        if (!automationSettingsRepository.isSmsCaptureEnabled.first()) return

        val parsed = parserRegistry.parse(sender, body, receivedAt) ?: return

        val dedupKey = parsed.referenceId ?: fallbackDedupKey(sender, body, receivedAt)
        if (transactionRepository.findBySourceReference(dedupKey) != null) return
        if (automationCandidateRepository.findBySourceReference(dedupKey) != null) return

        val accounts = accountRepository.observeAllAccounts().first()
        val fromResolution = resolveAccountHint(parsed.accountHint, accounts)
        val toResolution = parsed.counterAccountHint?.let { resolveAccountHint(it, accounts) }

        val accountsResolved = fromResolution is AccountResolution.Resolved &&
            (parsed.type != TransactionType.TRANSFER || toResolution is AccountResolution.Resolved)
        val confidence = if (parsed.baseConfidence == ParseConfidence.HIGH && accountsResolved) {
            ParseConfidence.HIGH
        } else {
            ParseConfidence.LOW
        }

        val categoryId = categorizeMerchant(parsed.merchant)
        val automaticAddEnabled = automationSettingsRepository.isAutomaticAddEnabled.first()

        if (confidence == ParseConfidence.HIGH && automaticAddEnabled) {
            autoAdd(parsed, dedupKey, (fromResolution as AccountResolution.Resolved).accountId, toResolution, categoryId)
        } else {
            queue(parsed, dedupKey, fromResolution, toResolution, categoryId, confidence)
        }
    }

    private suspend fun autoAdd(
        parsed: ParsedTransaction,
        dedupKey: String,
        accountId: String,
        toResolution: AccountResolution?,
        categoryId: String?,
    ) {
        val amountInput = minorUnitsToEditableString(parsed.amountMinor)

        val transactionId = if (parsed.type == TransactionType.TRANSFER) {
            val toAccountId = (toResolution as AccountResolution.Resolved).accountId
            createTransferUseCase(
                fromAccountId = accountId,
                toAccountId = toAccountId,
                amountInput = amountInput,
                note = parsed.merchant,
                date = parsed.occurredAt,
                source = TransactionSource.SMS,
                sourceReference = dedupKey,
            ).getOrNull()?.id
        } else {
            createTransactionUseCase(
                type = parsed.type,
                accountId = accountId,
                amountInput = amountInput,
                categoryId = categoryId,
                payee = parsed.merchant,
                note = null,
                date = parsed.occurredAt,
                source = TransactionSource.SMS,
                sourceReference = dedupKey,
            ).getOrNull()?.id?.also {
                if (parsed.type == TransactionType.EXPENSE) checkBudgetAlertsUseCase()
            }
        } ?: return

        val summary = describeAmount(parsed)
        createNotification(
            type = NotificationType.TRANSACTION_CAPTURED,
            title = "$summary captured",
            body = "$summary from ${parsed.provider} was added to your ledger. Tap to view or undo.",
            deepLink = "transactions/$transactionId/edit",
            relatedEntityType = "transaction",
            relatedEntityId = transactionId,
        )
    }

    private suspend fun queue(
        parsed: ParsedTransaction,
        dedupKey: String,
        fromResolution: AccountResolution,
        toResolution: AccountResolution?,
        categoryId: String?,
        confidence: ParseConfidence,
    ) {
        val candidate = AutomationCandidate(
            id = newId(),
            sourceType = TransactionSource.SMS,
            type = parsed.type,
            amountMinor = parsed.amountMinor,
            currency = parsed.currency,
            accountId = (fromResolution as? AccountResolution.Resolved)?.accountId,
            toAccountId = (toResolution as? AccountResolution.Resolved)?.accountId,
            categoryId = categoryId,
            payee = parsed.merchant,
            note = null,
            date = parsed.occurredAt,
            confidence = confidence,
            sourceReference = dedupKey,
            status = AutomationCandidateStatus.PENDING,
            createdAt = System.currentTimeMillis(),
        )
        automationCandidateRepository.create(candidate)

        val pendingCount = automationCandidateRepository.observePendingCount().first()
        createNotification(
            type = NotificationType.TRANSACTION_NEEDS_REVIEW,
            title = "${describeAmount(parsed)} needs review",
            body = if (pendingCount > 1) {
                "From ${parsed.provider}. $pendingCount transactions are waiting in the Review Queue."
            } else {
                "From ${parsed.provider}. Tap to review and confirm it."
            },
            deepLink = "review-queue",
            relatedEntityType = "automation_candidate",
            relatedEntityId = candidate.id,
        )
    }

    private fun describeAmount(parsed: ParsedTransaction): String = formatMoney(parsed.amountMinor, parsed.currency)

    private fun fallbackDedupKey(sender: String, body: String, receivedAt: Long): String {
        val minuteBucket = receivedAt / 60_000
        return "sms:${sender.trim()}:${body.trim().hashCode()}:$minuteBucket"
    }
}
