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
    val isIncome: Boolean,
    /** plan.md §22 — true for both legs of a transfer. */
    val isTransfer: Boolean = false,
    /** The other leg's account name, e.g. "Wallet" for a transfer shown on the "Cash" account. */
    val transferAccountName: String? = null,
    /** plan.md §13 — true when this transaction has split rows attached. */
    val isSplit: Boolean = false,
    val splitCategoryNames: List<String> = emptyList(),
    /** plan.md §16 — names of every label assigned to this transaction. */
    val labelNames: List<String> = emptyList(),
) {
    val title: String
        get() = when {
            isTransfer && transferAccountName != null ->
                if (isIncome) "Transfer from $transferAccountName" else "Transfer to $transferAccountName"
            else -> payee ?: categoryName ?: "Transaction"
        }

    val subtitle: String
        get() {
            val base = when {
                isTransfer -> accountName
                isSplit -> "Split • " + splitCategoryNames.joinToString(", ")
                else -> listOfNotNull(categoryName ?: "Uncategorized", accountName).joinToString(" • ")
            }
            return if (labelNames.isEmpty()) base else "$base • ${labelNames.joinToString(", ")}"
        }
}

data class TransactionGroupUi(
    val dateLabel: String,
    val transactions: List<TransactionUi>,
)
