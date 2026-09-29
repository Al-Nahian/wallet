package com.example.wallet.data.repository

import com.example.wallet.data.local.entity.TemplateEntity
import com.example.wallet.domain.model.Template

fun TemplateEntity.toDomain(): Template = Template(
    id = id,
    name = name,
    accountId = accountId,
    categoryId = categoryId,
    labelId = labelId,
    payee = payee,
    place = place,
    createdAt = createdAt,
)

fun Template.toEntity(): TemplateEntity = TemplateEntity(
    id = id,
    name = name,
    accountId = accountId,
    categoryId = categoryId,
    labelId = labelId,
    payee = payee,
    place = place,
    createdAt = createdAt,
)
