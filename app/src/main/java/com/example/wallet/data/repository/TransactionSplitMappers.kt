package com.example.wallet.data.repository

import com.example.wallet.data.local.entity.TransactionSplitEntity
import com.example.wallet.domain.model.TransactionSplit

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
