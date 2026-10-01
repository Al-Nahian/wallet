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

class CreateTransferUseCaseTest {

    private lateinit var accountRepository: FakeAccountRepository
    private lateinit var transactionRepository: FakeTransactionRepository
    private lateinit var useCase: CreateTransferUseCase

    @Before
    fun setUp() = runTest {
        accountRepository = FakeAccountRepository()
        transactionRepository = FakeTransactionRepository()
        useCase = CreateTransferUseCase(transactionRepository, accountRepository)

        accountRepository.create(account("cash", "Cash"))
        accountRepository.create(account("bkash", "bKash"))
    }

    private fun account(id: String, name: String, archived: Boolean = false) = Account(
        id = id,
        name = name,
        type = AccountType.CASH,
        institutionId = null,
        currency = "BDT",
        openingBalanceMinor = 0L,
        isArchived = archived,
        createdAt = 0L,
        updatedAt = 0L,
    )

    @Test
    fun `creates two linked legs, outgoing negative and incoming positive`() = runTest {
        val result = useCase("cash", "bkash", "20000", "rent", 1000L)

        assertTrue(result.isSuccess)
        val all = transactionRepository.observeTransactions().first()
        assertEquals(2, all.size)

        val outgoing = all.single { it.accountId == "cash" }
        val incoming = all.single { it.accountId == "bkash" }

        assertEquals(TransactionType.TRANSFER, outgoing.type)
        assertEquals(TransactionType.TRANSFER, incoming.type)
        assertEquals(-20_000_00L, outgoing.amountMinor)
        assertEquals(20_000_00L, incoming.amountMinor)
        assertNotNull(outgoing.transferId)
        assertEquals(outgoing.transferId, incoming.transferId)
        assertEquals(null, outgoing.categoryId)
        assertEquals(null, incoming.categoryId)
    }

    @Test
    fun `same account transfer is rejected`() = runTest {
        val result = useCase("cash", "cash", "1000", null, 1000L)

        assertTrue(result.isFailure)
        assertEquals(
            TransactionError.SameAccountTransfer,
            (result.exceptionOrNull() as TransactionValidationException).error,
        )
    }

    @Test
    fun `unknown destination account is rejected`() = runTest {
        val result = useCase("cash", "does-not-exist", "1000", null, 1000L)

        assertTrue(result.isFailure)
        assertEquals(
            TransactionError.DestinationAccountNotFound,
            (result.exceptionOrNull() as TransactionValidationException).error,
        )
    }

    @Test
    fun `archived destination account is rejected`() = runTest {
        accountRepository.create(account("archived-dest", "Old Wallet", archived = true))

        val result = useCase("cash", "archived-dest", "1000", null, 1000L)

        assertTrue(result.isFailure)
        assertEquals(
            TransactionError.DestinationAccountArchived,
            (result.exceptionOrNull() as TransactionValidationException).error,
        )
    }

    @Test
    fun `invalid amount is rejected and nothing is persisted`() = runTest {
        val result = useCase("cash", "bkash", "0", null, 1000L)

        assertTrue(result.isFailure)
        assertEquals(
            TransactionError.InvalidAmount,
            (result.exceptionOrNull() as TransactionValidationException).error,
        )
        assertTrue(transactionRepository.observeTransactions().first().isEmpty())
    }
}
