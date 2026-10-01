package com.expensetracker.wallet.domain.usecase.transaction

import com.expensetracker.wallet.domain.model.Account
import com.expensetracker.wallet.domain.model.AccountType
import com.expensetracker.wallet.domain.model.TransactionType
import com.expensetracker.wallet.domain.usecase.account.FakeAccountRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SplitTransactionUseCaseTest {

    private lateinit var accountRepository: FakeAccountRepository
    private lateinit var transactionRepository: FakeTransactionRepository
    private lateinit var splitRepository: FakeTransactionSplitRepository
    private lateinit var createUseCase: CreateTransactionUseCase
    private lateinit var splitUseCase: SplitTransactionUseCase
    private lateinit var transactionId: String

    @Before
    fun setUp() = runTest {
        accountRepository = FakeAccountRepository()
        transactionRepository = FakeTransactionRepository()
        splitRepository = FakeTransactionSplitRepository()
        createUseCase = CreateTransactionUseCase(transactionRepository, accountRepository)
        splitUseCase = SplitTransactionUseCase(transactionRepository, splitRepository)

        accountRepository.create(
            Account(
                id = "account-1",
                name = "Cash",
                type = AccountType.CASH,
                institutionId = null,
                currency = "BDT",
                openingBalanceMinor = 0L,
                isArchived = false,
                createdAt = 0L,
                updatedAt = 0L,
            ),
        )

        transactionId = createUseCase(
            TransactionType.EXPENSE, "account-1", "3000", null, "Agora", null, 1000L,
        ).getOrThrow().id
    }

    @Test
    fun `splits that sum to the parent amount are persisted`() = runTest {
        val result = splitUseCase(
            transactionId,
            listOf(
                SplitInput("groceries", 1_500_00L, null),
                SplitInput("household", 900_00L, null),
                SplitInput("personal-care", 600_00L, "toothpaste etc"),
            ),
        )

        assertTrue(result.isSuccess)
        val persisted = splitRepository.observeByTransaction(transactionId).first()
        assertEquals(3, persisted.size)
        assertEquals(300_000L, persisted.sumOf { it.amountMinor })
    }

    @Test
    fun `splits that don't sum to the parent amount are rejected`() = runTest {
        val result = splitUseCase(
            transactionId,
            listOf(
                SplitInput("groceries", 1_500_00L, null),
                SplitInput("household", 800_00L, null),
            ),
        )

        assertTrue(result.isFailure)
        assertEquals(
            TransactionError.SplitSumMismatch,
            (result.exceptionOrNull() as TransactionValidationException).error,
        )
        assertTrue(splitRepository.observeByTransaction(transactionId).first().isEmpty())
    }

    @Test
    fun `unknown transaction is rejected`() = runTest {
        val result = splitUseCase("does-not-exist", listOf(SplitInput("groceries", 100L, null)))

        assertTrue(result.isFailure)
        assertEquals(
            TransactionError.TransactionNotFound,
            (result.exceptionOrNull() as TransactionValidationException).error,
        )
    }

    @Test
    fun `replacing splits removes the previous set`() = runTest {
        splitUseCase(
            transactionId,
            listOf(SplitInput("groceries", 1_500_00L, null), SplitInput("household", 1_500_00L, null)),
        )

        val result = splitUseCase(
            transactionId,
            listOf(SplitInput("groceries", 3_000_00L, null)),
        )

        assertTrue(result.isSuccess)
        val persisted = splitRepository.observeByTransaction(transactionId).first()
        assertEquals(1, persisted.size)
        assertEquals(3_000_00L, persisted.single().amountMinor)
    }
}
