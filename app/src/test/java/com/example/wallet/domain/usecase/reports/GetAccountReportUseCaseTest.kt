package com.example.wallet.domain.usecase.reports

import com.example.wallet.domain.model.Account
import com.example.wallet.domain.model.AccountType
import com.example.wallet.domain.model.TransactionType
import com.example.wallet.domain.usecase.account.CalculateBalanceUseCase
import com.example.wallet.domain.usecase.account.FakeAccountRepository
import com.example.wallet.domain.usecase.transaction.CreateTransactionUseCase
import com.example.wallet.domain.usecase.transaction.FakeTransactionRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GetAccountReportUseCaseTest {

    private lateinit var transactionRepository: FakeTransactionRepository
    private lateinit var accountRepository: FakeAccountRepository
    private lateinit var createTransaction: CreateTransactionUseCase
    private lateinit var useCase: GetAccountReportUseCase

    private val account = Account(
        id = "cash", name = "Cash", type = AccountType.CASH, institutionId = null, currency = "BDT",
        openingBalanceMinor = 100_000_00L, isArchived = false, createdAt = 0L, updatedAt = 0L,
    )

    @Before
    fun setUp() = runTest {
        transactionRepository = FakeTransactionRepository()
        accountRepository = FakeAccountRepository()
        accountRepository.create(account)
        createTransaction = CreateTransactionUseCase(transactionRepository, accountRepository)
        useCase = GetAccountReportUseCase(transactionRepository, CalculateBalanceUseCase(transactionRepository))
    }

    @Test
    fun `reports balance and in-range inflow and outflow, excluding transactions outside the range`() = runTest {
        // In range (day 1000-2000): 5,000 income, 1,000 expense.
        createTransaction(TransactionType.INCOME, account.id, "5000", null, null, null, 1_500L)
        createTransaction(TransactionType.EXPENSE, account.id, "1000", null, null, null, 1_800L)
        // Outside range: shouldn't count toward inflow/outflow, but still affects the balance.
        createTransaction(TransactionType.EXPENSE, account.id, "2000", null, null, null, 5_000L)

        val report = useCase(account, startInclusive = 1_000L, endInclusive = 2_000L)

        assertEquals(5_000_00L, report.inflowMinor)
        assertEquals(1_000_00L, report.outflowMinor)
        assertEquals(4_000_00L, report.netMinor)
        // 100,000 + 5,000 - 1,000 - 2,000 = 102,000
        assertEquals(102_000_00L, report.currentBalanceMinor)
    }

    @Test
    fun `no transactions in range means zero inflow and outflow but balance is unaffected`() = runTest {
        val report = useCase(account, startInclusive = 1_000L, endInclusive = 2_000L)

        assertEquals(0L, report.inflowMinor)
        assertEquals(0L, report.outflowMinor)
        assertEquals(100_000_00L, report.currentBalanceMinor)
    }
}
