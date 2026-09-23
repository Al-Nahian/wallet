package com.example.wallet.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.wallet.domain.model.AutomationCandidateStatus
import com.example.wallet.domain.model.ParseConfidence
import com.example.wallet.domain.model.TransactionSource
import com.example.wallet.domain.model.TransactionType

/** plans/14-sms-notification-automation.md — the Review Queue's backing table. No foreign keys
 * to account/category: those may not have been resolved yet (see [AutomationCandidateEntity]'s
 * domain counterpart's doc comment), and a since-deleted account/category shouldn't cascade-wipe
 * queue history. */
@Entity(tableName = "automation_candidates", indices = [Index("status"), Index("sourceReference")])
data class AutomationCandidateEntity(
    @PrimaryKey val id: String,
    val sourceType: TransactionSource,
    val type: TransactionType,
    val amountMinor: Long,
    val currency: String,
    val accountId: String?,
    val toAccountId: String?,
    val categoryId: String?,
    val payee: String?,
    val note: String?,
    val date: Long,
    val confidence: ParseConfidence,
    val sourceReference: String?,
    val status: AutomationCandidateStatus,
    val createdAt: Long,
)
