package com.expensetracker.wallet.domain.usecase.budget

import com.expensetracker.wallet.domain.model.Budget
import com.expensetracker.wallet.domain.model.BudgetPeriod
import com.expensetracker.wallet.domain.model.Transaction
import com.expensetracker.wallet.domain.model.TransactionSplit
import com.expensetracker.wallet.domain.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Test

class CalculateBudgetUsageUseCaseTest {

    private val useCase = CalculateBudgetUsageUseCase()

    private fun budget(amountMinor: Long, start: Long = 1_000L, end: Long = 2_000L) = Budget(
        id = "budget-1", name = "Groceries", period = BudgetPeriod.MONTHLY, startDate = start, endDate = end,
        amountMinor = amountMinor, currency = "BDT", createdAt = 0L, updatedAt = 0L,
    )

    private fun expense(id: String, categoryId: String?, amountMinor: Long, date: Long = 1_500L) = Transaction(
        id = id, accountId = "account-1", type = TransactionType.EXPENSE, amountMinor = amountMinor,
        currency = "BDT", categoryId = categoryId, payee = null, note = null, date = date,
        createdAt = 0L, updatedAt = 0L, isRecurring = false, deletedAt = null,
    )

    @Test
    fun `an overall budget with no category filter counts every expense in range`() {
        val transactions = listOf(
            expense("tx-1", "groceries", 3_000_00L),
            expense("tx-2", "transport", 1_000_00L),
        )

        val usage = useCase(budget(10_000_00L), emptySet(), transactions, emptyList())

        assertEquals(4_000_00L, usage.spentMinor)
        assertEquals(6_000_00L, usage.remainingMinor)
        assertEquals(40.0, usage.usagePercent, 0.001)
    }

    @Test
    fun `a category-scoped budget only counts expenses in its category set`() {
        val transactions = listOf(
            expense("tx-1", "groceries", 3_000_00L),
            expense("tx-2", "transport", 1_000_00L),
        )

        val usage = useCase(budget(10_000_00L), setOf("groceries"), transactions, emptyList())

        assertEquals(3_000_00L, usage.spentMinor)
    }

    @Test
    fun `expenses outside the budget date range are excluded`() {
        val transactions = listOf(expense("tx-1", "groceries", 3_000_00L, date = 5_000L))

        val usage = useCase(budget(10_000_00L), emptySet(), transactions, emptyList())

        assertEquals(0L, usage.spentMinor)
    }

    @Test
    fun `income and transfers never count toward spend`() {
        val income = Transaction(
            id = "tx-income", accountId = "account-1", type = TransactionType.INCOME, amountMinor = 5_000_00L,
            currency = "BDT", categoryId = "groceries", payee = null, note = null, date = 1_500L,
            createdAt = 0L, updatedAt = 0L, isRecurring = false, deletedAt = null,
        )

        val usage = useCase(budget(10_000_00L), emptySet(), listOf(income), emptyList())

        assertEquals(0L, usage.spentMinor)
    }

    @Test
    fun `a split transaction attributes each split to its own category`() {
        val splitParent = expense("tx-split", null, 3_000_00L)
        val splits = listOf(
            TransactionSplit("split-1", "tx-split", "groceries", 2_000_00L, null),
            TransactionSplit("split-2", "tx-split", "transport", 1_000_00L, null),
        )

        val usage = useCase(budget(10_000_00L), setOf("groceries"), listOf(splitParent), splits)

        assertEquals(2_000_00L, usage.spentMinor)
    }

    @Test
    fun `usage percent is zero when the budget amount is non-positive rather than dividing by zero`() {
        val usage = useCase(budget(0L), emptySet(), emptyList(), emptyList())

        assertEquals(0.0, usage.usagePercent, 0.001)
    }
}
