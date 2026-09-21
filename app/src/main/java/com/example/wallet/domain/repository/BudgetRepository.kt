package com.example.wallet.domain.repository

import com.example.wallet.domain.model.Budget
import com.example.wallet.domain.model.BudgetCategory
import kotlinx.coroutines.flow.Flow

/** Implemented in Phase 10. */
interface BudgetRepository {
    fun observeBudgets(): Flow<List<Budget>>
    fun observeBudget(id: String): Flow<Budget?>
    fun observeBudgetCategories(budgetId: String): Flow<List<BudgetCategory>>
    suspend fun create(budget: Budget, categoryLimits: List<BudgetCategory>)
    suspend fun update(budget: Budget, categoryLimits: List<BudgetCategory>)
}
