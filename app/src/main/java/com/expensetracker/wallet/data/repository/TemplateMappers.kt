package com.expensetracker.wallet.data.repository

import com.expensetracker.wallet.data.local.entity.TemplateEntity
import com.expensetracker.wallet.domain.model.Template

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
