package com.example.wallet.domain.usecase.budget

import com.example.wallet.core.common.newId
import com.example.wallet.core.common.parseMoneyToMinorUnits
import com.example.wallet.domain.model.Budget
import com.example.wallet.domain.model.BudgetCategory
import com.example.wallet.domain.model.BudgetPeriod
import com.example.wallet.domain.repository.BudgetRepository
import javax.inject.Inject

/**
 * plan.md §17/§70. A budget can either track total expenses across every category ("overall" —
 * [categoryLimitInputs] empty) or a specific set of categories with their own per-category limit
 * — [BudgetRepository.create]/[CalculateBudgetUsageUseCase] both treat an empty category set as
 * "all expense categories count," so this is a deliberate supported mode, not an omission.
 */
class CreateBudgetUseCase @Inject constructor(
    private val budgetRepository: BudgetRepository,
) {
    suspend operator fun invoke(
        name: String,
        period: BudgetPeriod,
        startDate: Long,
        endDate: Long,
        amountInput: String,
        currency: String,
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

        val now = System.currentTimeMillis()
        val budget = Budget(
            id = newId(),
            name = trimmedName,
            period = period,
            startDate = startDate,
            endDate = endDate,
            amountMinor = amountMinor,
            currency = currency.trim().uppercase(),
            createdAt = now,
            updatedAt = now,
        )
        val budgetCategories = categoryLimits.map { (categoryId, limitMinor) ->
            BudgetCategory(budgetId = budget.id, categoryId = categoryId, limitMinor = limitMinor)
        }

        budgetRepository.create(budget, budgetCategories)
        return Result.success(budget)
    }
}
