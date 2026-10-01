package com.expensetracker.wallet.domain.usecase.dashboard

import com.expensetracker.wallet.domain.model.Account
import com.expensetracker.wallet.domain.model.AccountType
import com.expensetracker.wallet.domain.model.TransactionType
import com.expensetracker.wallet.domain.usecase.account.FakeAccountRepository
import com.expensetracker.wallet.domain.usecase.transaction.CreateTransactionUseCase
import com.expensetracker.wallet.domain.usecase.transaction.CreateTransferUseCase
import com.expensetracker.wallet.domain.usecase.transaction.FakeTransactionRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GetMonthlyIncomeExpensesUseCaseTest {

    private lateinit var accountRepository: FakeAccountRepository
    private lateinit var transactionRepository: FakeTransactionRepository
    private lateinit var createTransactionUseCase: CreateTransactionUseCase
    private lateinit var createTransferUseCase: CreateTransferUseCase
    private lateinit var getMonthlyIncome: GetMonthlyIncomeUseCase
    private lateinit var getMonthlyExpenses: GetMonthlyExpensesUseCase

    @Before
    fun setUp() = runTest {
        accountRepository = FakeAccountRepository()
        transactionRepository = FakeTransactionRepository()
        createTransactionUseCase = CreateTransactionUseCase(transactionRepository, accountRepository)
        createTransferUseCase = CreateTransferUseCase(transactionRepository, accountRepository)
        getMonthlyIncome = GetMonthlyIncomeUseCase(transactionRepository)
        getMonthlyExpenses = GetMonthlyExpensesUseCase(transactionRepository)

        accountRepository.create(
            Account("account-1", "Cash", AccountType.CASH, null, "BDT", 0L, false, 0L, 0L),
        )
        accountRepository.create(
            Account("account-2", "Wallet", AccountType.CASH, null, "BDT", 0L, false, 0L, 0L),
        )
    }

    @Test
    fun `sums income and expenses within range, excluding transfers`() = runTest {
        createTransactionUseCase(TransactionType.INCOME, "account-1", "50000", null, null, null, date = 500L)
        createTransactionUseCase(TransactionType.EXPENSE, "account-1", "20000", null, null, null, date = 600L)
        createTransferUseCase("account-1", "account-2", "10000", null, 500L)
        // Outside the queried range.
        createTransactionUseCase(TransactionType.INCOME, "account-1", "99999", null, null, null, date = 5000L)

        assertEquals(50_000_00L, getMonthlyIncome(0L, 1000L))
        assertEquals(20_000_00L, getMonthlyExpenses(0L, 1000L))
    }

    @Test
    fun `zero when nothing recorded in range`() = runTest {
        assertEquals(0L, getMonthlyIncome(0L, 1000L))
        assertEquals(0L, getMonthlyExpenses(0L, 1000L))
    }
}
