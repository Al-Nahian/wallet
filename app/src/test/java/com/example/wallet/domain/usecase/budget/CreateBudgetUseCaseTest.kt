package com.example.wallet.domain.usecase.budget

import com.example.wallet.domain.model.BudgetPeriod
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CreateBudgetUseCaseTest {

    private lateinit var budgetRepository: FakeBudgetRepository
    private lateinit var useCase: CreateBudgetUseCase

    @Before
    fun setUp() {
        budgetRepository = FakeBudgetRepository()
        useCase = CreateBudgetUseCase(budgetRepository)
    }

    @Test
    fun `creates an overall budget with no category limits`() = runTest {
        val result = useCase(
            name = "Monthly spending",
            period = BudgetPeriod.MONTHLY,
            startDate = 1_000L,
            endDate = 2_000L,
            amountInput = "30000",
            currency = "bdt",
        )

        assertTrue(result.isSuccess)
        val budget = result.getOrThrow()
        assertEquals(30_000_00L, budget.amountMinor)
        assertEquals("BDT", budget.currency)
        assertEquals(budget, budgetRepository.observeBudgets().first().single())
        assertTrue(budgetRepository.observeBudgetCategories(budget.id).first().isEmpty())
    }

    @Test
    fun `creates a budget with per-category limits`() = runTest {
        val result = useCase(
            name = "Monthly spending",
            period = BudgetPeriod.MONTHLY,
            startDate = 1_000L,
            endDate = 2_000L,
            amountInput = "30000",
            currency = "BDT",
            categoryLimitInputs = listOf("groceries" to "10000", "transport" to "5000"),
        )

        val budget = result.getOrThrow()
        val limits = budgetRepository.observeBudgetCategories(budget.id).first()
        assertEquals(2, limits.size)
        assertEquals(10_000_00L, limits.single { it.categoryId == "groceries" }.limitMinor)
    }

    @Test
    fun `blank name fails validation`() = runTest {
        val result = useCase("  ", BudgetPeriod.MONTHLY, 1_000L, 2_000L, "30000", "BDT")

        assertTrue(result.isFailure)
        assertEquals(BudgetError.NameRequired, (result.exceptionOrNull() as BudgetValidationException).error)
    }

    @Test
    fun `end date not after start date fails validation`() = runTest {
        val result = useCase("Budget", BudgetPeriod.MONTHLY, 2_000L, 2_000L, "30000", "BDT")

        assertEquals(BudgetError.InvalidDateRange, (result.exceptionOrNull() as BudgetValidationException).error)
    }

    @Test
    fun `non-positive amount fails validation`() = runTest {
        val result = useCase("Budget", BudgetPeriod.MONTHLY, 1_000L, 2_000L, "0", "BDT")

        assertEquals(BudgetError.InvalidAmount, (result.exceptionOrNull() as BudgetValidationException).error)
    }

    @Test
    fun `invalid category limit fails validation`() = runTest {
        val result = useCase(
            "Budget", BudgetPeriod.MONTHLY, 1_000L, 2_000L, "30000", "BDT",
            categoryLimitInputs = listOf("groceries" to "not-a-number"),
        )

        assertEquals(BudgetError.InvalidCategoryLimit, (result.exceptionOrNull() as BudgetValidationException).error)
    }
}
