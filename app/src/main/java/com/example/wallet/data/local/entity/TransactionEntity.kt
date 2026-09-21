package com.example.wallet.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.wallet.domain.model.TransactionType

/**
 * plan.md §12. Only the "Initial model" fields plus `deletedAt` (§44 soft delete, needed
 * from Phase 5 onward) are included here. §12's "Future fields" (source, confidence,
 * isReviewed, externalId, transferId, version, cross-currency fields...) are deliberately
 * left out until the phase that actually needs them adds it via a real migration —
 * Phase 6 adds `transferId`, Phase 14 adds `source`/`confidence`/`isReviewed`/`externalId`,
 * Phase 17 adds `version`.
 */
@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [
        Index("accountId"),
        Index("date"),
        Index("categoryId"),
        Index("payee"),
        Index("type"),
    ],
)
data class TransactionEntity(
    @PrimaryKey val id: String,
    val accountId: String,
    val type: TransactionType,
    val amountMinor: Long,
    val currency: String,
    val categoryId: String? = null,
    val payee: String? = null,
    val note: String? = null,
    val date: Long,
    val createdAt: Long,
    val updatedAt: Long,
    val isRecurring: Boolean = false,
    val deletedAt: Long? = null,
)
