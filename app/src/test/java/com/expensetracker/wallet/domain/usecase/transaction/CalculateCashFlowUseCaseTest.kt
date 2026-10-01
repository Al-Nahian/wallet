package com.expensetracker.wallet.domain.usecase.transaction

import com.expensetracker.wallet.domain.model.Account
import com.expensetracker.wallet.domain.model.AccountType
import com.expensetracker.wallet.domain.model.TransactionType
import com.expensetracker.wallet.domain.usecase.account.FakeAccountRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class CalculateCashFlowUseCaseTest {

    private lateinit var accountRepository: FakeAccountRepository
    private lateinit var transactionRepository: FakeTransactionRepository
    private lateinit var createUseCase: CreateTransactionUseCase
    private lateinit var createTransferUseCase: CreateTransferUseCase
    private lateinit var cashFlowUseCase: CalculateCashFlowUseCase

    @Before
    fun setUp() = runTest {
        accountRepository = FakeAccountRepository()
        transactionRepository = FakeTransactionRepository()
        createUseCase = CreateTransactionUseCase(transactionRepository, accountRepository)
        createTransferUseCase = CreateTransferUseCase(transactionRepository, accountRepository)
        cashFlowUseCase = CalculateCashFlowUseCase(transactionRepository)

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
    fun `transfers are excluded from cash flow`() = runTest {
        createUseCase(TransactionType.INCOME, "account-1", "50000", null, null, null, date = 500L)
        createUseCase(TransactionType.EXPENSE, "account-1", "20000", null, null, null, date = 500L)
        createTransferUseCase("account-1", "account-2", "10000", null, 500L)

        val cashFlow = cashFlowUseCase(startInclusive = 0L, endInclusive = 1000L)

        assertEquals(30_000_00L, cashFlow)
    }

    @Test
    fun `cash flow is income minus expenses within the date range`() = runTest {
        createUseCase(TransactionType.INCOME, "account-1", "50000", null, null, null, date = 500L)
        createUseCase(TransactionType.EXPENSE, "account-1", "20000", null, null, null, date = 600L)
        // Outside the queried range — must not be counted.
        createUseCase(TransactionType.INCOME, "account-1", "99999", null, null, null, date = 5000L)

        val cashFlow = cashFlowUseCase(startInclusive = 0L, endInclusive = 1000L)

        assertEquals(30_000_00L, cashFlow)
    }

    @Test
    fun `excludes soft-deleted transactions`() = runTest {
        val toDelete = createUseCase(
            TransactionType.INCOME, "account-1", "50000", null, null, null, date = 500L,
        ).getOrThrow()
        createUseCase(TransactionType.EXPENSE, "account-1", "10000", null, null, null, date = 500L)

        transactionRepository.delete(toDelete.id)

        val cashFlow = cashFlowUseCase(startInclusive = 0L, endInclusive = 1000L)

        assertEquals(-10_000_00L, cashFlow)
    }

    @Test
    fun `zero when there are no transactions in range`() = runTest {
        assertEquals(0L, cashFlowUseCase(0L, 1000L))
    }
}
