package com.example.wallet.domain.usecase.reports

import com.example.wallet.domain.usecase.dashboard.CategorySpend
import org.junit.Assert.assertEquals
import org.junit.Test

class GetCategoryTrendUseCaseTest {

    private val useCase = GetCategoryTrendUseCase()

    private fun spend(categoryId: String, name: String, amountMinor: Long) =
        CategorySpend(categoryId, name, amountMinor, 0.0)

    @Test
    fun `combines current, previous and trailing averages per category`() {
        val current = listOf(spend("groceries", "Groceries", 3_000_00L))
        val previous = listOf(spend("groceries", "Groceries", 2_000_00L))
        val last3 = listOf(
            listOf(spend("groceries", "Groceries", 3_000_00L)),
            listOf(spend("groceries", "Groceries", 2_000_00L)),
            listOf(spend("groceries", "Groceries", 1_000_00L)),
        )
        val last6 = last3 + listOf(
            listOf(spend("groceries", "Groceries", 1_000_00L)),
            listOf(spend("groceries", "Groceries", 1_000_00L)),
            listOf(spend("groceries", "Groceries", 1_000_00L)),
        )

        val result = useCase(current, previous, last3, last6)

        val trend = result.single { it.categoryId == "groceries" }
        assertEquals(3_000_00L, trend.currentMonthMinor)
        assertEquals(2_000_00L, trend.previousMonthMinor)
        assertEquals(2_000_00L, trend.avg3MonthMinor) // (3000+2000+1000)/3
        assertEquals(1_500_00L, trend.avg6MonthMinor) // (3000+2000+1000+1000+1000+1000)/6
    }

    @Test
    fun `a category with no spend this month but spend in the past still appears`() {
        val current = emptyList<CategorySpend>()
        val previous = listOf(spend("transport", "Transportation", 500_00L))
        val last3 = listOf(emptyList(), previous, emptyList())
        val last6 = last3 + listOf(emptyList(), emptyList(), emptyList())

        val result = useCase(current, previous, last3, last6)

        val trend = result.single { it.categoryId == "transport" }
        assertEquals(0L, trend.currentMonthMinor)
        assertEquals(500_00L, trend.previousMonthMinor)
    }

    @Test
    fun `no history anywhere produces an empty list`() {
        val result = useCase(emptyList(), emptyList(), List(3) { emptyList() }, List(6) { emptyList() })

        assertEquals(emptyList<CategoryTrend>(), result)
    }
}
