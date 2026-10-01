package com.expensetracker.wallet.domain.usecase.budget

import com.expensetracker.wallet.core.common.parseMoneyToMinorUnits
import com.expensetracker.wallet.domain.model.Budget
import com.expensetracker.wallet.domain.model.BudgetCategory
import com.expensetracker.wallet.domain.model.BudgetPeriod
import com.expensetracker.wallet.domain.repository.BudgetRepository
import javax.inject.Inject

class UpdateBudgetUseCase @Inject constructor(
    private val budgetRepository: BudgetRepository,
) {
    suspend operator fun invoke(
        existing: Budget,
        name: String,
        period: BudgetPeriod,
        startDate: Long,
        endDate: Long,
        amountInput: String,
        categoryLimitInputs: List<Pair<String, String>> = emptyList(),
    ): Result<Budget> {
        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) {
            return Result.failure(BudgetValidationException(BudgetError.NameRequired))
        }
        if (endDate <= startDate) {
            return Result.failure(BudgetValidationException(BudgetError.InvalidDateRange))
        }
        val amountMinor = parseMoneyToMinorUnits(amountInput)?.takeIf { it > 0 }
            ?: return Result.failure(BudgetValidationException(BudgetError.InvalidAmount))

        val categoryLimits = mutableListOf<Pair<String, Long>>()
        for ((categoryId, limitInput) in categoryLimitInputs) {
            val limitMinor = parseMoneyToMinorUnits(limitInput)?.takeIf { it > 0 }
                ?: return Result.failure(BudgetValidationException(BudgetError.InvalidCategoryLimit))
            categoryLimits += categoryId to limitMinor
        }

        val budget = existing.copy(
            name = trimmedName,
            period = period,
            startDate = startDate,
            endDate = endDate,
            amountMinor = amountMinor,
            updatedAt = System.currentTimeMillis(),
        )
        val budgetCategories = categoryLimits.map { (categoryId, limitMinor) ->
            BudgetCategory(budgetId = budget.id, categoryId = categoryId, limitMinor = limitMinor)
        }

        budgetRepository.update(budget, budgetCategories)
        return Result.success(budget)
    }
}
