package com.example.wallet.domain.usecase.transaction

import com.example.wallet.domain.model.Account
import com.example.wallet.domain.model.AccountType
import com.example.wallet.domain.model.TransactionType
import com.example.wallet.domain.usecase.account.FakeAccountRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DeleteTransactionUseCaseTest {

    private lateinit var accountRepository: FakeAccountRepository
    private lateinit var transactionRepository: FakeTransactionRepository
    private lateinit var createUseCase: CreateTransactionUseCase
    private lateinit var deleteUseCase: DeleteTransactionUseCase

    @Before
    fun setUp() = runTest {
        accountRepository = FakeAccountRepository()
        transactionRepository = FakeTransactionRepository()
        createUseCase = CreateTransactionUseCase(transactionRepository, accountRepository)
        deleteUseCase = DeleteTransactionUseCase(transactionRepository)

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
    fun `soft-deletes the transaction, excluded from observeTransactions but queryable directly`() = runTest {
        val transaction = createUseCase(
            TransactionType.EXPENSE, "account-1", "500", null, null, null, 1000L,
        ).getOrThrow()

        val result = deleteUseCase(transaction.id)

        assertTrue(result.isSuccess)
        assertTrue(transactionRepository.observeTransactions().first().isEmpty())
        val stillThere = transactionRepository.getTransaction(transaction.id)
        assertNotNull(stillThere?.deletedAt)
    }

    @Test
    fun `deleting an unknown transaction fails with TransactionNotFound`() = runTest {
        val result = deleteUseCase("does-not-exist")

        assertTrue(result.isFailure)
        assertEquals(
            TransactionError.TransactionNotFound,
            (result.exceptionOrNull() as TransactionValidationException).error,
        )
    }
}
