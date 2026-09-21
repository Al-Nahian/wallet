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
)

data class TransactionSplit(
    val id: String,
    val transactionId: String,
    val categoryId: String,
    val amountMinor: Long,
    val note: String?,
)
