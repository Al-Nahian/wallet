package com.expensetracker.wallet.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/** plan.md §17 — per-category limit within a budget. */
@Entity(
    tableName = "budget_categories",
    primaryKeys = ["budgetId", "categoryId"],
    foreignKeys = [
        ForeignKey(
            entity = BudgetEntity::class,
            parentColumns = ["id"],
            childColumns = ["budgetId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("budgetId"), Index("categoryId")],
)
data class BudgetCategoryEntity(
    val budgetId: String,
    val categoryId: String,
    val limitMinor: Long,
)
