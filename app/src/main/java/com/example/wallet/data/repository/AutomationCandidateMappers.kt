package com.example.wallet.data.repository

import com.example.wallet.data.local.entity.AutomationCandidateEntity
import com.example.wallet.domain.model.AutomationCandidate

fun AutomationCandidateEntity.toDomain(): AutomationCandidate = AutomationCandidate(
    id = id,
    sourceType = sourceType,
    type = type,
    amountMinor = amountMinor,
    currency = currency,
    accountId = accountId,
    toAccountId = toAccountId,
    categoryId = categoryId,
    payee = payee,
    note = note,
    date = date,
    confidence = confidence,
    sourceReference = sourceReference,
    status = status,
    createdAt = createdAt,
)

fun AutomationCandidate.toEntity(): AutomationCandidateEntity = AutomationCandidateEntity(
    id = id,
    sourceType = sourceType,
    type = type,
    amountMinor = amountMinor,
    currency = currency,
    accountId = accountId,
    toAccountId = toAccountId,
    categoryId = categoryId,
    payee = payee,
    note = note,
    date = date,
    confidence = confidence,
    sourceReference = sourceReference,
    status = status,
    createdAt = createdAt,
)
