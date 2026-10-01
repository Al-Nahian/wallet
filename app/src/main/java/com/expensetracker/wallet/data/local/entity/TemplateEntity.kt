package com.expensetracker.wallet.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** A saved transaction-form shortcut (fixed account + category + label, optional payee/place).
 * All three FKs cascade on delete — a template referencing a deleted account, category, or label
 * can no longer be applied to anything, same reasoning as `transactions.accountId`'s cascade. */
@Entity(
    tableName = "templates",
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
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = LabelEntity::class,
            parentColumns = ["id"],
            childColumns = ["labelId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("accountId"), Index("categoryId"), Index("labelId")],
)
data class TemplateEntity(
    @PrimaryKey val id: String,
    val name: String,
    val accountId: String,
    val categoryId: String,
    val labelId: String,
    val payee: String?,
    val place: String?,
    val createdAt: Long,
)
