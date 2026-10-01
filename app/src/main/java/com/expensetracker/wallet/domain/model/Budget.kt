package com.expensetracker.wallet.domain.model

data class Budget(
    val id: String,
    val name: String,
    val period: BudgetPeriod,
    val startDate: Long,
    val endDate: Long,
    val amountMinor: Long,
    val currency: String,
    val createdAt: Long,
    val updatedAt: Long,
)

data class BudgetCategory(
    val budgetId: String,
    val categoryId: String,
    val limitMinor: Long,
)
