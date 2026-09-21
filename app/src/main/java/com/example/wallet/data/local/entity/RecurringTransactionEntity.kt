package com.example.wallet.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.wallet.domain.model.RecurringFrequency
import com.example.wallet.domain.model.TransactionType

/** plan.md §18 */
@Entity(
    tableName = "recurring_transactions",
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
    indices = [Index("accountId"), Index("categoryId"), Index("nextDate")],
)
data class RecurringTransactionEntity(
    @PrimaryKey val id: String,
    val accountId: String,
    val categoryId: String? = null,
    val amountMinor: Long,
    val currency: String,
    val frequency: RecurringFrequency,
    val nextDate: Long,
    val endDate: Long? = null,
    val type: TransactionType,
    val payee: String? = null,
    val note: String? = null,
    val isActive: Boolean = true,
)
