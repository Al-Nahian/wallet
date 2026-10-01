package com.expensetracker.wallet.data.repository

import com.expensetracker.wallet.data.local.entity.AccountEntity
import com.expensetracker.wallet.domain.model.Account

fun AccountEntity.toDomain(): Account = Account(
    id = id,
    name = name,
    type = type,
    institutionId = institutionId,
    currency = currency,
    openingBalanceMinor = openingBalanceMinor,
    isArchived = isArchived,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun Account.toEntity(): AccountEntity = AccountEntity(
    id = id,
    name = name,
    type = type,
    institutionId = institutionId,
    currency = currency,
    openingBalanceMinor = openingBalanceMinor,
    isArchived = isArchived,
    createdAt = createdAt,
    updatedAt = updatedAt,
)
