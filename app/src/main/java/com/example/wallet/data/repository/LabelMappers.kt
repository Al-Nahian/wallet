package com.example.wallet.data.repository

import com.example.wallet.data.local.entity.LabelEntity
import com.example.wallet.domain.model.Label

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
