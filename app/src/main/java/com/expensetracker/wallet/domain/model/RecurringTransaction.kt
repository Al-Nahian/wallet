package com.expensetracker.wallet.domain.model

data class RecurringTransaction(
    val id: String,
    val accountId: String,
    val categoryId: String?,
    val amountMinor: Long,
    val currency: String,
    val frequency: RecurringFrequency,
    val nextDate: Long,
    val endDate: Long?,
    val type: TransactionType,
    val payee: String?,
    val note: String?,
    val isActive: Boolean,
    /** true: `GenerateDueRecurringTransactionsUseCase` posts a real Transaction the moment this
     * rule is due. false: it only reminds the user (a `RECURRING_DUE` notification) and leaves
     * them to record it manually. Either way `nextDate` advances once the rule is processed. */
    val autoPost: Boolean = true,
)
