package com.expensetracker.wallet.domain.usecase.transaction

import com.expensetracker.wallet.domain.model.Account
import com.expensetracker.wallet.domain.model.AccountType
import com.expensetracker.wallet.domain.model.TransactionType
import com.expensetracker.wallet.domain.usecase.account.FakeAccountRepository
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
    private lateinit var createTransferUseCase: CreateTransferUseCase
    private lateinit var deleteUseCase: DeleteTransactionUseCase

    @Before
    fun setUp() = runTest {
        accountRepository = FakeAccountRepository()
        transactionRepository = FakeTransactionRepository()
        createUseCase = CreateTransactionUseCase(transactionRepository, accountRepository)
        createTransferUseCase = CreateTransferUseCase(transactionRepository, accountRepository)
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
        accountRepository.create(
            Account(
                id = "account-2",
                name = "Wallet",
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
    fun `deleting one leg of a transfer soft-deletes both legs atomically`() = runTest {
        createTransferUseCase("account-1", "account-2", "10000", null, 1000L)
        val outgoing = transactionRepository.observeTransactions().first().single { it.accountId == "account-1" }
        val incoming = transactionRepository.observeTransactions().first().single { it.accountId == "account-2" }

        val result = deleteUseCase(outgoing.id)

        assertTrue(result.isSuccess)
        assertTrue(transactionRepository.observeTransactions().first().isEmpty())
        assertNotNull(transactionRepository.getTransaction(outgoing.id)?.deletedAt)
        assertNotNull(transactionRepository.getTransaction(incoming.id)?.deletedAt)
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
