package com.expensetracker.wallet.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** plan.md §13. `SUM(splits.amountMinor) == transaction.amountMinor` is enforced by
 * `SplitValidator` (domain layer), not by the database. */
@Entity(
    tableName = "transaction_splits",
    foreignKeys = [
        ForeignKey(
            entity = TransactionEntity::class,
            parentColumns = ["id"],
            childColumns = ["transactionId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("transactionId"), Index("categoryId")],
)
data class TransactionSplitEntity(
    @PrimaryKey val id: String,
    val transactionId: String,
    val categoryId: String,
    val amountMinor: Long,
    val note: String? = null,
)
