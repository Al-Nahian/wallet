package com.expensetracker.wallet.data.repository

import com.expensetracker.wallet.data.local.entity.NotificationEntity
import com.expensetracker.wallet.domain.model.Notification

fun NotificationEntity.toDomain(): Notification = Notification(
    id = id,
    type = type,
    title = title,
    body = body,
    deepLink = deepLink,
    createdAt = createdAt,
    readAt = readAt,
    relatedEntityType = relatedEntityType,
    relatedEntityId = relatedEntityId,
)

fun Notification.toEntity(): NotificationEntity = NotificationEntity(
    id = id,
    type = type,
    title = title,
    body = body,
    deepLink = deepLink,
    createdAt = createdAt,
    readAt = readAt,
    relatedEntityType = relatedEntityType,
    relatedEntityId = relatedEntityId,
)
