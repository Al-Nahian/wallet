package com.example.wallet.domain.usecase.dashboard

import com.example.wallet.domain.model.Category
import com.example.wallet.domain.model.Transaction
import com.example.wallet.domain.model.TransactionSplit
import com.example.wallet.domain.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GetCategorySpendUseCaseTest {

    private val useCase = GetCategorySpendUseCase()

    private val groceries = Category("groceries", "group-1", "Groceries", null, 0, true)
    private val transport = Category("transport", "group-2", "Transportation", null, 0, true)
    private val categoriesById = mapOf(groceries.id to groceries, transport.id to transport)

    private fun expense(id: String, categoryId: String?, amountMinor: Long) = Transaction(
        id = id, accountId = "account-1", type = TransactionType.EXPENSE, amountMinor = amountMinor,
        currency = "BDT", categoryId = categoryId, payee = null, note = null, date = 1000L,
        createdAt = 0L, updatedAt = 0L, isRecurring = false, deletedAt = null,
    )

    @Test
    fun `groups expenses by category and computes percentages`() {
        val transactions = listOf(
            expense("tx-1", groceries.id, 3_000_00L),
            expense("tx-2", transport.id, 1_000_00L),
        )

        val result = useCase(transactions, emptyList(), categoriesById)

        assertEquals(2, result.size)
        val groceriesSpend = result.single { it.categoryId == groceries.id }
        assertEquals(3_000_00L, groceriesSpend.amountMinor)
        assertEquals(75.0, groceriesSpend.percentage, 0.01)
        val transportSpend = result.single { it.categoryId == transport.id }
        assertEquals(1_000_00L, transportSpend.amountMinor)
        assertEquals(25.0, transportSpend.percentage, 0.01)
    }

    @Test
    fun `a split transaction attributes each split to its own category, not the parent`() {
        val split = expense("tx-split", null, 3_000_00L)
        val splits = listOf(
            TransactionSplit("split-1", "tx-split", groceries.id, 1_500_00L, null),
            TransactionSplit("split-2", "tx-split", transport.id, 1_500_00L, null),
        )

        val result = useCase(listOf(split), splits, categoriesById)

        assertEquals(2, result.size)
        assertEquals(1_500_00L, result.single { it.categoryId == groceries.id }.amountMinor)
        assertEquals(1_500_00L, result.single { it.categoryId == transport.id }.amountMinor)
    }

    @Test
    fun `income and transfers are excluded from the breakdown`() {
        val income = Transaction(
            id = "tx-income", accountId = "account-1", type = TransactionType.INCOME, amountMinor = 5_000_00L,
            currency = "BDT", categoryId = groceries.id, payee = null, note = null, date = 1000L,
            createdAt = 0L, updatedAt = 0L, isRecurring = false, deletedAt = null,
        )
        val transfer = Transaction(
            id = "tx-transfer", accountId = "account-1", type = TransactionType.TRANSFER, amountMinor = -2_000_00L,
            currency = "BDT", categoryId = null, payee = null, note = null, date = 1000L,
            createdAt = 0L, updatedAt = 0L, isRecurring = false, deletedAt = null, transferId = "transfer-1",
        )

        val result = useCase(listOf(income, transfer), emptyList(), categoriesById)

        assertEquals(emptyList<CategorySpend>(), result)
    }

    @Test
    fun `no category becomes Uncategorized`() {
        val result = useCase(listOf(expense("tx-1", null, 500_00L)), emptyList(), categoriesById)

        assertEquals("Uncategorized", result.single().categoryName)
    }
}
