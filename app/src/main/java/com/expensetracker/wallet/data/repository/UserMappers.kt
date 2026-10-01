package com.expensetracker.wallet.data.repository

import com.expensetracker.wallet.data.local.entity.UserEntity
import com.expensetracker.wallet.domain.model.User

fun UserEntity.toDomain(): User = User(
    id = id,
    displayName = displayName,
    avatarUrl = avatarUrl,
    createdAt = createdAt,
    updatedAt = updatedAt,
)
