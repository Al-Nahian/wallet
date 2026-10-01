package com.expensetracker.wallet.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** plan.md §19 */
@Entity(
    tableName = "goals",
    foreignKeys = [
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [Index("accountId")],
)
data class GoalEntity(
    @PrimaryKey val id: String,
    val name: String,
    val targetAmountMinor: Long,
    val currentAmountMinor: Long,
    val currency: String,
    val targetDate: Long? = null,
    val accountId: String? = null,
    val createdAt: Long,
    val updatedAt: Long,
)
