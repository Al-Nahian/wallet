package com.example.wallet.domain.model

/**
 * plan.md §34/§87 (plans/14-sms-notification-automation.md) — a parsed SMS/notification
 * transaction that didn't clear the high-confidence bar, held for the user to confirm in the
 * Review Queue. [accountId]/[toAccountId] are null when the source account couldn't be resolved
 * unambiguously (e.g. more than one bank account) — the Review Queue screen requires the user to
 * pick one before it can be accepted. Never stores the raw SMS/notification text (plan.md §30/§50
 * — only the already-extracted structured fields).
 */
data class AutomationCandidate(
    val id: String,
    val sourceType: TransactionSource,
    val type: TransactionType,
    val amountMinor: Long,
    val currency: String,
    val accountId: String?,
    val toAccountId: String?,
    val categoryId: String?,
    val payee: String?,
    val note: String?,
    val date: Long,
    val confidence: ParseConfidence,
    val sourceReference: String?,
    val status: AutomationCandidateStatus,
    val createdAt: Long,
)
