package com.example.wallet.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.wallet.domain.model.NotificationType

/** plan.md §86 — added in migration v1 -> v2 (Phase 4). */
@Entity(tableName = "notifications", indices = [Index("readAt"), Index("createdAt")])
data class NotificationEntity(
    @PrimaryKey val id: String,
    val type: NotificationType,
    val title: String,
    val body: String,
    val deepLink: String? = null,
    val createdAt: Long,
    val readAt: Long? = null,
    val relatedEntityType: String? = null,
    val relatedEntityId: String? = null,
)
