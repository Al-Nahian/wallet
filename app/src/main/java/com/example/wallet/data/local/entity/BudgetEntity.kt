package com.example.wallet.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.wallet.domain.model.BudgetPeriod

/** plan.md §17 */
@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey val id: String,
    val name: String,
    val period: BudgetPeriod,
    val startDate: Long,
    val endDate: Long,
    val amountMinor: Long,
    val currency: String,
    val createdAt: Long,
    val updatedAt: Long,
)
