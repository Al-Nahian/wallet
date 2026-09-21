package com.example.wallet.data.repository

import com.example.wallet.data.local.entity.InstitutionEntity
import com.example.wallet.domain.model.Institution

fun InstitutionEntity.toDomain(): Institution = Institution(
    id = id,
    name = name,
    type = type,
    logo = logo,
    country = country,
)
