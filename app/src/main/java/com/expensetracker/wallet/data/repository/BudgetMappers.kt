package com.expensetracker.wallet.data.repository

import com.expensetracker.wallet.data.local.entity.BudgetCategoryEntity
import com.expensetracker.wallet.data.local.entity.BudgetEntity
import com.expensetracker.wallet.domain.model.Budget
import com.expensetracker.wallet.domain.model.BudgetCategory

fun BudgetEntity.toDomain(): Budget = Budget(
    id = id,
    name = name,
    period = period,
    startDate = startDate,
    endDate = endDate,
    amountMinor = amountMinor,
    currency = currency,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun Budget.toEntity(): BudgetEntity = BudgetEntity(
    id = id,
    name = name,
    period = period,
    startDate = startDate,
    endDate = endDate,
    amountMinor = amountMinor,
    currency = currency,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun BudgetCategoryEntity.toDomain(): BudgetCategory = BudgetCategory(
    budgetId = budgetId,
    categoryId = categoryId,
    limitMinor = limitMinor,
)

fun BudgetCategory.toEntity(): BudgetCategoryEntity = BudgetCategoryEntity(
    budgetId = budgetId,
    categoryId = categoryId,
    limitMinor = limitMinor,
)
