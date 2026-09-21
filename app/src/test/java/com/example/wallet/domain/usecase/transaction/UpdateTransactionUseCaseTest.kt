package com.example.wallet.domain.usecase.transaction

import com.example.wallet.domain.model.Account
import com.example.wallet.domain.model.AccountType
import com.example.wallet.domain.model.TransactionType
import com.example.wallet.domain.usecase.account.FakeAccountRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UpdateTransactionUseCaseTest {

    private lateinit var accountRepository: FakeAccountRepository
    private lateinit var transactionRepository: FakeTransactionRepository
    private lateinit var createUseCase: CreateTransactionUseCase
    private lateinit var updateUseCase: UpdateTransactionUseCase

    @Before
    fun setUp() = runTest {
        accountRepository = FakeAccountRepository()
        transactionRepository = FakeTransactionRepository()
        createUseCase = CreateTransactionUseCase(transactionRepository, accountRepository)
        updateUseCase = UpdateTransactionUseCase(transactionRepository, accountRepository)

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
    }

    @Test
    fun `updates amount and type`() = runTest {
        val original = createUseCase(
            TransactionType.EXPENSE, "account-1", "500", null, "Agora", null, 1000L,
        ).getOrThrow()

        val result = updateUseCase(
            transactionId = original.id,
            type = TransactionType.INCOME,
            accountId = "account-1",
            amountInput = "750.25",
            categoryId = null,
            payee = "Refund",
            note = null,
            date = 1000L,
        )

        assertTrue(result.isSuccess)
        val updated = result.getOrThrow()
        assertEquals(TransactionType.INCOME, updated.type)
        assertEquals(75_025L, updated.amountMinor)
        assertEquals("Refund", updated.payee)
    }

    @Test
    fun `unknown transaction fails with TransactionNotFound`() = runTest {
        val result = updateUseCase("missing", TransactionType.EXPENSE, "account-1", "100", null, null, null, 1000L)

        assertTrue(result.isFailure)
        assertEquals(
            TransactionError.TransactionNotFound,
            (result.exceptionOrNull() as TransactionValidationException).error,
        )
    }

    @Test
    fun `invalid amount is rejected`() = runTest {
        val original = createUseCase(
            TransactionType.EXPENSE, "account-1", "500", null, null, null, 1000L,
        ).getOrThrow()

        val result = updateUseCase(original.id, TransactionType.EXPENSE, "account-1", "abc", null, null, null, 1000L)

        assertTrue(result.isFailure)
        assertEquals(
            TransactionError.InvalidAmount,
            (result.exceptionOrNull() as TransactionValidationException).error,
        )
    }
}
