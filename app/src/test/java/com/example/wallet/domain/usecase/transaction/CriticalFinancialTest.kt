package com.example.wallet.domain.usecase.transaction

import com.example.wallet.domain.model.Account
import com.example.wallet.domain.model.AccountType
import com.example.wallet.domain.model.TransactionType
import com.example.wallet.domain.usecase.account.CalculateBalanceUseCase
import com.example.wallet.domain.usecase.account.FakeAccountRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * plan.md §52 — the plan's own worked example, implemented exactly:
 * Opening 100,000 + income 50,000 - expenses 20,000, plus a transfer of 10,000 between two
 * owned accounts => total financial position 130,000; source account -10,000, destination
 * account +10,000; the transfer counts as neither income nor expense.
 */
class CriticalFinancialTest {

    private lateinit var accountRepository: FakeAccountRepository
    private lateinit var transactionRepository: FakeTransactionRepository
    private lateinit var createTransactionUseCase: CreateTransactionUseCase
    private lateinit var createTransferUseCase: CreateTransferUseCase
    private lateinit var calculateBalance: CalculateBalanceUseCase
    private lateinit var calculateCashFlow: CalculateCashFlowUseCase

    private lateinit var source: Account
    private lateinit var destination: Account

    @Before
    fun setUp() = runTest {
        accountRepository = FakeAccountRepository()
        transactionRepository = FakeTransactionRepository()
        createTransactionUseCase = CreateTransactionUseCase(transactionRepository, accountRepository)
        createTransferUseCase = CreateTransferUseCase(transactionRepository, accountRepository)
        calculateBalance = CalculateBalanceUseCase(transactionRepository)
        calculateCashFlow = CalculateCashFlowUseCase(transactionRepository)

        source = Account(
            id = "source",
            name = "Source",
            type = AccountType.CASH,
            institutionId = null,
            currency = "BDT",
            openingBalanceMinor = 100_000_00L,
            isArchived = false,
            createdAt = 0L,
            updatedAt = 0L,
        )
        destination = source.copy(id = "destination", name = "Destination", openingBalanceMinor = 0L)
        accountRepository.create(source)
        accountRepository.create(destination)
    }

    @Test
    fun `section 52 scenario matches exactly`() = runTest {
        createTransactionUseCase(TransactionType.INCOME, source.id, "50000", null, null, null, 500L).getOrThrow()
        createTransactionUseCase(TransactionType.EXPENSE, source.id, "20000", null, null, null, 600L).getOrThrow()
        createTransferUseCase(source.id, destination.id, "10000", null, 700L).getOrThrow()

        val sourceBalance = calculateBalance(source)
        val destinationBalance = calculateBalance(destination)

        // Source: 100,000 + 50,000 - 20,000 - 10,000 (transfer out) = 120,000
        assertEquals(120_000_00L, sourceBalance)
        // Destination: 0 + 10,000 (transfer in) = 10,000
        assertEquals(10_000_00L, destinationBalance)

        val totalPosition = sourceBalance + destinationBalance
        assertEquals(130_000_00L, totalPosition)

        // §52: "Source account: -10,000, Destination account: +10,000" relative to their
        // pre-transfer balances (110,000 income/expense-only and 0, respectively).
        val sourceBeforeTransfer = source.openingBalanceMinor + 50_000_00L - 20_000_00L
        assertEquals(-10_000_00L, sourceBalance - sourceBeforeTransfer)
        assertEquals(10_000_00L, destinationBalance)

        val cashFlow = calculateCashFlow(0L, 1000L)
        assertEquals(30_000_00L, cashFlow) // income 50,000 - expense 20,000; transfer excluded
        assertTrue(cashFlow != -10_000_00L) // sanity: the transfer amount never leaks into cash flow
    }
}
