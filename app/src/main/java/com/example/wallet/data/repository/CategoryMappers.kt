package com.example.wallet.data.repository

import com.example.wallet.data.local.entity.CategoryEntity
import com.example.wallet.data.local.entity.CategoryGroupEntity
import com.example.wallet.domain.model.Category
import com.example.wallet.domain.model.CategoryGroup

fun CategoryGroupEntity.toDomain(): CategoryGroup = CategoryGroup(
    id = id,
    name = name,
    color = color,
    icon = icon,
    type = type,
    sortOrder = sortOrder,
    isSystem = isSystem,
)

fun CategoryEntity.toDomain(): Category = Category(
    id = id,
    groupId = groupId,
    name = name,
    icon = icon,
    sortOrder = sortOrder,
    isSystem = isSystem,
)

fun Category.toEntity(): CategoryEntity = CategoryEntity(
    id = id,
    groupId = groupId,
    name = name,
    icon = icon,
    sortOrder = sortOrder,
    isSystem = isSystem,
)
