package com.expensetracker.wallet.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.expensetracker.wallet.domain.model.TransactionSource
import com.expensetracker.wallet.domain.model.TransactionType

/**
 * plan.md §12. Only the "Initial model" fields plus `deletedAt` (§44 soft delete, needed
 * from Phase 5 onward), `transferId` (§22, Phase 6), and `source`/`sourceReference` (Phase 14,
 * plans/14-sms-notification-automation.md) are included here. §12's remaining "Future fields"
 * (`confidence`, `isReviewed` — only ever relevant to a not-yet-committed candidate, so they live
 * on `AutomationCandidateEntity` instead of here; `version`) are deliberately left out until the
 * phase that actually needs them adds it via a real migration — Phase 17 adds `version`.
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
        Index("transferId"),
        Index("recurringTransactionId"),
        Index("sourceReference"),
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
    val transferId: String? = null,
    /** plans/11-recurring-goals.md, added in MIGRATION_3_4 — the rule that auto-posted this row,
     * if any. No FK: the rule may later be deleted while its already-posted history stays. */
    val recurringTransactionId: String? = null,
    val source: TransactionSource = TransactionSource.MANUAL,
    val sourceReference: String? = null,
    val place: String? = null,
)
