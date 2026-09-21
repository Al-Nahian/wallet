package com.example.wallet.data.repository

import com.example.wallet.data.local.entity.UserEntity
import com.example.wallet.domain.model.User

fun UserEntity.toDomain(): User = User(
    id = id,
    displayName = displayName,
    avatarUrl = avatarUrl,
    createdAt = createdAt,
    updatedAt = updatedAt,
)
