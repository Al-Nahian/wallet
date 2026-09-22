package com.example.wallet.domain.usecase.budget

import com.example.wallet.domain.model.Budget
import com.example.wallet.domain.model.BudgetCategory
import com.example.wallet.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeBudgetRepository : BudgetRepository {
    private val budgets = MutableStateFlow<Map<String, Budget>>(emptyMap())
    private val categoryLimits = MutableStateFlow<Map<String, List<BudgetCategory>>>(emptyMap())

    override fun observeBudgets(): Flow<List<Budget>> = budgets.map { it.values.toList() }

    override fun observeBudget(id: String): Flow<Budget?> = budgets.map { it[id] }

    override fun observeBudgetCategories(budgetId: String): Flow<List<BudgetCategory>> =
        categoryLimits.map { it[budgetId].orEmpty() }

    override suspend fun create(budget: Budget, categoryLimits: List<BudgetCategory>) {
        budgets.value = budgets.value + (budget.id to budget)
        this.categoryLimits.value = this.categoryLimits.value + (budget.id to categoryLimits)
    }

    override suspend fun update(budget: Budget, categoryLimits: List<BudgetCategory>) {
        budgets.value = budgets.value + (budget.id to budget)
        this.categoryLimits.value = this.categoryLimits.value + (budget.id to categoryLimits)
    }
}
