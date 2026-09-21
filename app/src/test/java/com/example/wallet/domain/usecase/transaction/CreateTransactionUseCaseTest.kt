package com.example.wallet.domain.usecase.transaction

import com.example.wallet.domain.model.Account
import com.example.wallet.domain.model.AccountType
import com.example.wallet.domain.model.TransactionType
import com.example.wallet.domain.usecase.account.FakeAccountRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CreateTransactionUseCaseTest {

    private lateinit var accountRepository: FakeAccountRepository
    private lateinit var transactionRepository: FakeTransactionRepository
    private lateinit var useCase: CreateTransactionUseCase
    private lateinit var account: Account

    @Before
    fun setUp() = runTest {
        accountRepository = FakeAccountRepository()
        transactionRepository = FakeTransactionRepository()
        useCase = CreateTransactionUseCase(transactionRepository, accountRepository)

        account = Account(
            id = "account-1",
            name = "Cash",
            type = AccountType.CASH,
            institutionId = null,
            currency = "BDT",
            openingBalanceMinor = 100_000_00,
            isArchived = false,
            createdAt = 0L,
            updatedAt = 0L,
        )
        accountRepository.create(account)
    }

    @Test
    fun `valid expense is created and persisted`() = runTest {
        val result = useCase(
            type = TransactionType.EXPENSE,
            accountId = account.id,
            amountInput = "1500.50",
            categoryId = null,
            payee = "Agora",
            note = null,
            date = 1000L,
        )

        assertTrue(result.isSuccess)
        val transaction = result.getOrThrow()
        assertEquals(150_050L, transaction.amountMinor)
        assertEquals("BDT", transaction.currency)
        assertEquals("Agora", transaction.payee)
        assertEquals(transaction, transactionRepository.observeTransactions().first().single())
    }

    @Test
    fun `valid income is created`() = runTest {
        val result = useCase(TransactionType.INCOME, account.id, "50000", null, null, null, 1000L)

        assertTrue(result.isSuccess)
        assertEquals(TransactionType.INCOME, result.getOrThrow().type)
    }

    @Test
    fun `zero amount is rejected`() = runTest {
        val result = useCase(TransactionType.EXPENSE, account.id, "0", null, null, null, 1000L)

        assertTrue(result.isFailure)
        assertEquals(
            TransactionError.InvalidAmount,
            (result.exceptionOrNull() as TransactionValidationException).error,
        )
    }

    @Test
    fun `negative amount is rejected`() = runTest {
        val result = useCase(TransactionType.EXPENSE, account.id, "-500", null, null, null, 1000L)

        assertTrue(result.isFailure)
        assertEquals(
            TransactionError.InvalidAmount,
            (result.exceptionOrNull() as TransactionValidationException).error,
        )
    }

    @Test
    fun `unknown account is rejected`() = runTest {
        val result = useCase(TransactionType.EXPENSE, "does-not-exist", "100", null, null, null, 1000L)

        assertTrue(result.isFailure)
        assertEquals(
            TransactionError.AccountNotFound,
            (result.exceptionOrNull() as TransactionValidationException).error,
        )
    }

    @Test
    fun `archived account is rejected`() = runTest {
        val archived = account.copy(id = "archived-1", isArchived = true)
        accountRepository.create(archived)

        val result = useCase(TransactionType.EXPENSE, archived.id, "100", null, null, null, 1000L)

        assertTrue(result.isFailure)
        assertEquals(
            TransactionError.AccountArchived,
            (result.exceptionOrNull() as TransactionValidationException).error,
        )
    }
}
