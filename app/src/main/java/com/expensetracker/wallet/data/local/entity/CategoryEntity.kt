package com.expensetracker.wallet.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** plan.md §14/§15 */
@Entity(
    tableName = "categories",
    foreignKeys = [
        ForeignKey(
            entity = CategoryGroupEntity::class,
            parentColumns = ["id"],
            childColumns = ["groupId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("groupId")],
)
data class CategoryEntity(
    @PrimaryKey val id: String,
    val groupId: String,
    val name: String,
    val icon: String? = null,
    val sortOrder: Int,
    val isSystem: Boolean,
)
