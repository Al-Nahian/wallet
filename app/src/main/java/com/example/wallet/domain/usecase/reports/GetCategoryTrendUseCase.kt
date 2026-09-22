package com.example.wallet.domain.usecase.reports

import com.example.wallet.domain.usecase.dashboard.CategorySpend
import javax.inject.Inject

data class CategoryTrend(
    val categoryId: String?,
    val categoryName: String,
    val currentMonthMinor: Long,
    val previousMonthMinor: Long,
    val avg3MonthMinor: Long,
    val avg6MonthMinor: Long,
)

/**
 * plan.md §24/§25 `getCategoryTrend()` — current month vs. previous month vs. trailing 3-/6-month
 * average, so a spike in a category reads against its own recent baseline rather than a single
 * prior month. Pure function over already-computed [CategorySpend] breakdowns (one list per
 * month) so it stays independently unit-testable; the caller is responsible for building each
 * month's breakdown (e.g. via `GetCategorySpendUseCase` filtered to that month's transactions).
 */
class GetCategoryTrendUseCase @Inject constructor() {
    operator fun invoke(
        currentMonth: List<CategorySpend>,
        previousMonth: List<CategorySpend>,
        last3Months: List<List<CategorySpend>>,
        last6Months: List<List<CategorySpend>>,
    ): List<CategoryTrend> {
        val names = LinkedHashMap<String?, String>()
        val ids = LinkedHashSet<String?>()
        for (spend in currentMonth) {
            ids += spend.categoryId
            names[spend.categoryId] = spend.categoryName
        }
        for (spend in previousMonth) {
            ids += spend.categoryId
            names[spend.categoryId] = spend.categoryName
        }
        for (month in last6Months) {
            for (spend in month) {
                ids += spend.categoryId
                names[spend.categoryId] = spend.categoryName
            }
        }

        val currentById = currentMonth.associateBy { it.categoryId }
        val previousById = previousMonth.associateBy { it.categoryId }

        return ids.map { categoryId ->
            CategoryTrend(
                categoryId = categoryId,
                categoryName = names[categoryId] ?: "Uncategorized",
                currentMonthMinor = currentById[categoryId]?.amountMinor ?: 0L,
                previousMonthMinor = previousById[categoryId]?.amountMinor ?: 0L,
                avg3MonthMinor = average(last3Months, categoryId),
                avg6MonthMinor = average(last6Months, categoryId),
            )
        }.sortedByDescending { it.currentMonthMinor }
    }

    private fun average(months: List<List<CategorySpend>>, categoryId: String?): Long {
        if (months.isEmpty()) return 0L
        val total = months.sumOf { month -> month.firstOrNull { it.categoryId == categoryId }?.amountMinor ?: 0L }
        return total / months.size
    }
}
