package com.example.wallet.data.repository

import com.example.wallet.data.local.entity.TransactionEntity
import com.example.wallet.domain.model.Transaction

fun TransactionEntity.toDomain(): Transaction = Transaction(
    id = id,
    accountId = accountId,
    type = type,
    amountMinor = amountMinor,
    currency = currency,
    categoryId = categoryId,
    payee = payee,
    note = note,
    date = date,
    createdAt = createdAt,
    updatedAt = updatedAt,
    isRecurring = isRecurring,
    deletedAt = deletedAt,
    transferId = transferId,
    recurringTransactionId = recurringTransactionId,
    source = source,
    sourceReference = sourceReference,
)

fun Transaction.toEntity(): TransactionEntity = TransactionEntity(
    id = id,
    accountId = accountId,
    type = type,
    amountMinor = amountMinor,
    currency = currency,
    categoryId = categoryId,
    payee = payee,
    note = note,
    date = date,
    createdAt = createdAt,
    updatedAt = updatedAt,
    isRecurring = isRecurring,
    deletedAt = deletedAt,
    transferId = transferId,
    recurringTransactionId = recurringTransactionId,
    source = source,
    sourceReference = sourceReference,
)
