package com.expensetracker.wallet.domain.model

data class Transaction(
    val id: String,
    val accountId: String,
    val type: TransactionType,
    val amountMinor: Long,
    val currency: String,
    val categoryId: String?,
    val payee: String?,
    val note: String?,
    val date: Long,
    val createdAt: Long,
    val updatedAt: Long,
    val isRecurring: Boolean,
    val deletedAt: Long?,
    /** plan.md §22 — links the two legs of a transfer (Phase 6). Null for every other type. */
    val transferId: String? = null,
    /** plans/11-recurring-goals.md — set when this row was auto-posted by
     * `GenerateDueRecurringTransactionsUseCase` from a `RecurringTransaction` rule; null for
     * every manually-entered transaction. */
    val recurringTransactionId: String? = null,
    /** plan.md §74/§87 (plans/14-sms-notification-automation.md) — MANUAL unless this row was
     * auto-added by the SMS/notification capture pipeline. */
    val source: TransactionSource = TransactionSource.MANUAL,
    /** The parser's extracted reference (e.g. a bKash TrxID) when [source] isn't MANUAL, or a
     * fallback hash of sender+body+timestamp when the message had no explicit reference — the
     * dedup key `ProcessIncomingSmsUseCase` checks before ever creating a second transaction for
     * the same real-world event (plan.md §33). Null for manual entries. */
    val sourceReference: String? = null,
    /** Free-text location, e.g. "Agora, Dhanmondi" — optional, user-entered only (never inferred
     * from a parsed SMS/notification). */
    val place: String? = null,
)

data class TransactionSplit(
    val id: String,
    val transactionId: String,
    val categoryId: String,
    val amountMinor: Long,
    val note: String?,
)
