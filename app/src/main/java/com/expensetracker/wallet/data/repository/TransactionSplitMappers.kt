package com.expensetracker.wallet.data.repository

import com.expensetracker.wallet.data.local.entity.TransactionSplitEntity
import com.expensetracker.wallet.domain.model.TransactionSplit

fun TransactionSplitEntity.toDomain(): TransactionSplit = TransactionSplit(
    id = id,
    transactionId = transactionId,
    categoryId = categoryId,
    amountMinor = amountMinor,
    note = note,
)

fun TransactionSplit.toEntity(): TransactionSplitEntity = TransactionSplitEntity(
    id = id,
    transactionId = transactionId,
    categoryId = categoryId,
    amountMinor = amountMinor,
    note = note,
)
