package com.example.wallet.domain.model

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
)
