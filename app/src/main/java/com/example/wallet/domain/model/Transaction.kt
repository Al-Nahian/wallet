package com.example.wallet.domain.model

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
)

data class TransactionSplit(
    val id: String,
    val transactionId: String,
    val categoryId: String,
    val amountMinor: Long,
    val note: String?,
)
