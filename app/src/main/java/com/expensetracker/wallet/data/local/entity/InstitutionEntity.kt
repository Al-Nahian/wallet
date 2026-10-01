package com.expensetracker.wallet.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** plan.md §11 */
@Entity(tableName = "institutions")
data class InstitutionEntity(
    @PrimaryKey val id: String,
    val name: String,
    val type: String,
    val logo: String? = null,
    val country: String? = null,
)
