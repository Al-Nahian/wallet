package com.expensetracker.wallet.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** plan.md §29 — merchant normalization, used starting Phase 14. */
@Entity(tableName = "merchants")
data class MerchantEntity(
    @PrimaryKey val id: String,
    val canonicalName: String,
)
