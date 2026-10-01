package com.expensetracker.wallet.domain.usecase.category

import com.expensetracker.wallet.domain.model.Transaction
import com.expensetracker.wallet.domain.model.TransactionType
import com.expensetracker.wallet.domain.usecase.transaction.FakeTransactionRepository
import com.expensetracker.wallet.domain.usecase.transaction.TransactionError
import com.expensetracker.wallet.domain.usecase.transaction.TransactionValidationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AssignCategoryUseCaseTest {

    private lateinit var transactionRepository: FakeTransactionRepository
    private lateinit var useCase: AssignCategoryUseCase
    private lateinit var transaction: Transaction

    @Before
    fun setUp() = runTest {
        transactionRepository = FakeTransactionRepository()
        useCase = AssignCategoryUseCase(transactionRepository)

        transaction = Transaction(
            id = "tx-1",
            accountId = "account-1",
            type = TransactionType.EXPENSE,
            amountMinor = 500_00L,
            currency = "BDT",
            categoryId = "old-category",
            payee = null,
            note = null,
            date = 1000L,
            createdAt = 0L,
            updatedAt = 0L,
            isRecurring = false,
            deletedAt = null,
        )
        transactionRepository.create(transaction)
    }

    @Test
    fun `reassigns the transaction's category`() = runTest {
        val result = useCase("tx-1", "new-category")

        assertTrue(result.isSuccess)
        assertEquals("new-category", transactionRepository.getTransaction("tx-1")?.categoryId)
    }

    @Test
    fun `can clear the category back to uncategorized`() = runTest {
        val result = useCase("tx-1", null)

        assertTrue(result.isSuccess)
        assertEquals(null, transactionRepository.getTransaction("tx-1")?.categoryId)
    }

    @Test
    fun `unknown transaction is rejected`() = runTest {
        val result = useCase("does-not-exist", "new-category")

        assertTrue(result.isFailure)
        assertEquals(
            TransactionError.TransactionNotFound,
            (result.exceptionOrNull() as TransactionValidationException).error,
        )
    }
}
