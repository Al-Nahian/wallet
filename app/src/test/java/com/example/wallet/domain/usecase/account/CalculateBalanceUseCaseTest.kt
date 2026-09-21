package com.example.wallet.domain.usecase.account

import com.example.wallet.domain.model.Account
import com.example.wallet.domain.model.AccountType
import com.example.wallet.domain.model.TransactionType
import com.example.wallet.domain.usecase.transaction.CreateTransactionUseCase
import com.example.wallet.domain.usecase.transaction.FakeTransactionRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class CalculateBalanceUseCaseTest {

    private lateinit var accountRepository: FakeAccountRepository
    private lateinit var transactionRepository: FakeTransactionRepository
    private lateinit var createTransactionUseCase: CreateTransactionUseCase
    private lateinit var useCase: CalculateBalanceUseCase
    private lateinit var account: Account

    @Before
    fun setUp() = runTest {
        accountRepository = FakeAccountRepository()
        transactionRepository = FakeTransactionRepository()
        createTransactionUseCase = CreateTransactionUseCase(transactionRepository, accountRepository)
        useCase = CalculateBalanceUseCase(transactionRepository)

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
    fun `balance equals opening balance when no transactions exist`() = runTest {
        assertEquals(100_000_00L, useCase(account))
    }

    @Test
    fun `balance is opening balance plus income minus expenses`() = runTest {
        createTransactionUseCase(TransactionType.INCOME, account.id, "50000", null, null, null, 1000L)
        createTransactionUseCase(TransactionType.EXPENSE, account.id, "20000", null, null, null, 1000L)
        createTransactionUseCase(TransactionType.EXPENSE, account.id, "10000", null, null, null, 1000L)

        // opening 100,000 + income 50,000 - expense 20,000 - expense 10,000 = 120,000
        assertEquals(120_000_00L, useCase(account))
    }

    @Test
    fun `soft-deleted transactions are excluded from the balance`() = runTest {
        val expense = createTransactionUseCase(
            TransactionType.EXPENSE, account.id, "20000", null, null, null, 1000L,
        ).getOrThrow()
        transactionRepository.delete(expense.id)

        assertEquals(100_000_00L, useCase(account))
    }

    @Test
    fun `only sums transactions for the given account`() = runTest {
        val otherAccount = account.copy(id = "account-2")
        accountRepository.create(otherAccount)

        createTransactionUseCase(TransactionType.EXPENSE, account.id, "20000", null, null, null, 1000L)
        createTransactionUseCase(TransactionType.INCOME, otherAccount.id, "99999", null, null, null, 1000L)

        // opening 100,000 - expense 20,000 = 80,000 (the other account's income must not count)
        assertEquals(80_000_00L, useCase(account))
    }
}
