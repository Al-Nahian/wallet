package com.example.wallet.feature.transactions

import com.example.wallet.domain.model.Account
import com.example.wallet.domain.model.Category
import com.example.wallet.domain.model.Label
import com.example.wallet.domain.model.Transaction
import com.example.wallet.domain.model.TransactionSplit
import com.example.wallet.domain.model.TransactionType

/**
 * Shared by [TransactionsViewModel] and [com.example.wallet.feature.accounts.AccountDetailViewModel]
 * so a transaction (including a transfer leg or a split parent) renders identically wherever it
 * appears.
 */
fun Transaction.toTransactionUi(
    accountsById: Map<String, Account>,
    categoriesById: Map<String, Category>,
    splitsByTransaction: Map<String, List<TransactionSplit>>,
    transferLegsByTransferId: Map<String?, List<Transaction>>,
    labelsByTransaction: Map<String, List<Label>> = emptyMap(),
): TransactionUi {
    val isTransfer = type == TransactionType.TRANSFER
    val sibling = if (isTransfer) {
        transferLegsByTransferId[transferId]?.firstOrNull { it.id != id }
    } else {
        null
    }
    val splits = splitsByTransaction[id].orEmpty()

    return TransactionUi(
        id = id,
        type = type,
        amountMinor = kotlin.math.abs(amountMinor),
        currency = currency,
        categoryName = categoryId?.let { categoriesById[it]?.name },
        payee = payee,
        accountName = accountsById[accountId]?.name ?: "Unknown account",
        date = date,
        isIncome = if (isTransfer) amountMinor > 0 else type == TransactionType.INCOME,
        isTransfer = isTransfer,
        transferAccountName = sibling?.let { accountsById[it.accountId]?.name },
        isSplit = splits.isNotEmpty(),
        splitCategoryNames = splits.mapNotNull { categoriesById[it.categoryId]?.name },
        labelNames = labelsByTransaction[id].orEmpty().map { it.name },
    )
}
