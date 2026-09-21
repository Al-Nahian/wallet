package com.example.wallet.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.wallet.domain.model.CategoryType

/** plan.md §14/§15 */
@Entity(tableName = "category_groups")
data class CategoryGroupEntity(
    @PrimaryKey val id: String,
    val name: String,
    val color: String,
    val icon: String? = null,
    val type: CategoryType,
    val sortOrder: Int,
    val isSystem: Boolean,
)
