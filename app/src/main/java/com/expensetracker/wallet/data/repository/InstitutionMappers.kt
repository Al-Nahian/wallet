package com.expensetracker.wallet.data.repository

import com.expensetracker.wallet.data.local.entity.InstitutionEntity
import com.expensetracker.wallet.domain.model.Institution

fun InstitutionEntity.toDomain(): Institution = Institution(
    id = id,
    name = name,
    type = type,
    logo = logo,
    country = country,
)
