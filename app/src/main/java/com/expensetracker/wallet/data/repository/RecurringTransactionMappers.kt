package com.expensetracker.wallet.data.repository

import com.expensetracker.wallet.data.local.entity.RecurringTransactionEntity
import com.expensetracker.wallet.domain.model.RecurringTransaction

fun RecurringTransactionEntity.toDomain(): RecurringTransaction = RecurringTransaction(
    id = id,
    accountId = accountId,
    categoryId = categoryId,
    amountMinor = amountMinor,
    currency = currency,
    frequency = frequency,
    nextDate = nextDate,
    endDate = endDate,
    type = type,
    payee = payee,
    note = note,
    isActive = isActive,
    autoPost = autoPost,
)

fun RecurringTransaction.toEntity(): RecurringTransactionEntity = RecurringTransactionEntity(
    id = id,
    accountId = accountId,
    categoryId = categoryId,
    amountMinor = amountMinor,
    currency = currency,
    frequency = frequency,
    nextDate = nextDate,
    endDate = endDate,
    type = type,
    payee = payee,
    note = note,
    isActive = isActive,
    autoPost = autoPost,
)
