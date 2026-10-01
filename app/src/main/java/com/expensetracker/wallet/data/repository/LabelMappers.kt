package com.expensetracker.wallet.data.repository

import com.expensetracker.wallet.data.local.entity.LabelEntity
import com.expensetracker.wallet.domain.model.Label

fun LabelEntity.toDomain(): Label = Label(
    id = id,
    name = name,
    color = color,
    createdAt = createdAt,
)

fun Label.toEntity(): LabelEntity = LabelEntity(
    id = id,
    name = name,
    color = color,
    createdAt = createdAt,
)
