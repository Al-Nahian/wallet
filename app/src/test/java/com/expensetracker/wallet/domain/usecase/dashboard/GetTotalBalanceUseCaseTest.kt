package com.expensetracker.wallet.domain.usecase.dashboard

import com.expensetracker.wallet.domain.model.Account
import com.expensetracker.wallet.domain.model.AccountType
import com.expensetracker.wallet.domain.model.TransactionType
import com.expensetracker.wallet.domain.usecase.account.CalculateBalanceUseCase
import com.expensetracker.wallet.domain.usecase.account.FakeAccountRepository
import com.expensetracker.wallet.domain.usecase.transaction.CreateTransactionUseCase
import com.expensetracker.wallet.domain.usecase.transaction.FakeTransactionRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GetTotalBalanceUseCaseTest {

    private lateinit var accountRepository: FakeAccountRepository
    private lateinit var transactionRepository: FakeTransactionRepository
    private lateinit var createTransactionUseCase: CreateTransactionUseCase
    private lateinit var useCase: GetTotalBalanceUseCase

    @Before
    fun setUp() {
        accountRepository = FakeAccountRepository()
        transactionRepository = FakeTransactionRepository()
        createTransactionUseCase = CreateTransactionUseCase(transactionRepository, accountRepository)
        useCase = GetTotalBalanceUseCase(CalculateBalanceUseCase(transactionRepository))
    }

    private fun account(id: String, opening: Long) = Account(
        id = id, name = id, type = AccountType.CASH, institutionId = null, currency = "BDT",
        openingBalanceMinor = opening, isArchived = false, createdAt = 0L, updatedAt = 0L,
    )

    @Test
    fun `sums balances across every given account`() = runTest {
        val cash = account("cash", 100_000_00L)
        val wallet = account("wallet", 20_000_00L)
        accountRepository.create(cash)
        accountRepository.create(wallet)
        createTransactionUseCase(TransactionType.EXPENSE, "cash", "10000", null, null, null, 1000L)

        val total = useCase(listOf(cash, wallet))

        // 100,000 - 10,000 + 20,000 = 110,000
        assertEquals(110_000_00L, total)
    }

    @Test
    fun `zero accounts means zero balance`() = runTest {
        assertEquals(0L, useCase(emptyList()))
    }
}
