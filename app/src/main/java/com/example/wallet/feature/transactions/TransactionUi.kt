package com.example.wallet.feature.transactions

import com.example.wallet.domain.model.TransactionType

data class TransactionUi(
    val id: String,
    val type: TransactionType,
    val amountMinor: Long,
    val currency: String,
    val categoryName: String?,
    val payee: String?,
    val accountName: String,
    val date: Long,
) {
    val title: String get() = payee ?: categoryName ?: "Transaction"
    val subtitle: String get() = listOfNotNull(categoryName ?: "Uncategorized", accountName).joinToString(" • ")
    val isIncome: Boolean get() = type == TransactionType.INCOME
}

data class TransactionGroupUi(
    val dateLabel: String,
    val transactions: List<TransactionUi>,
)
