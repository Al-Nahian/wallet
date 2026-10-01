package com.expensetracker.wallet.domain.usecase.budget

import com.expensetracker.wallet.core.lifecycle.AppForegroundTracker
import com.expensetracker.wallet.domain.model.Budget
import com.expensetracker.wallet.domain.model.BudgetPeriod
import com.expensetracker.wallet.domain.model.NotificationType
import com.expensetracker.wallet.domain.model.Transaction
import com.expensetracker.wallet.domain.model.TransactionType
import com.expensetracker.wallet.domain.usecase.notification.CreateNotificationUseCase
import com.expensetracker.wallet.domain.usecase.notification.FakeNotificationPoster
import com.expensetracker.wallet.domain.usecase.notification.FakeNotificationRepository
import com.expensetracker.wallet.domain.usecase.transaction.FakeTransactionRepository
import com.expensetracker.wallet.domain.usecase.transaction.FakeTransactionSplitRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CheckBudgetAlertsUseCaseTest {

    private lateinit var budgetRepository: FakeBudgetRepository
    private lateinit var transactionRepository: FakeTransactionRepository
    private lateinit var splitRepository: FakeTransactionSplitRepository
    private lateinit var notificationRepository: FakeNotificationRepository
    private lateinit var useCase: CheckBudgetAlertsUseCase

    // Realistic wall-clock-scale timestamps, not small arbitrary millis — the use case dedups
    // existing notifications by checking their (real, System.currentTimeMillis()-based)
    // createdAt against the budget's own start/end dates, which only lines up when the budget's
    // dates are themselves real epoch millis, exactly as they are in production.
    private val now = System.currentTimeMillis()
    private val periodStart = now - 10_000L
    private val periodEnd = now + 10_000L
    private val txDate = now - 1_000L

    private val budget = Budget(
        id = "budget-1", name = "Groceries", period = BudgetPeriod.MONTHLY, startDate = periodStart, endDate = periodEnd,
        amountMinor = 10_000_00L, currency = "BDT", createdAt = 0L, updatedAt = 0L,
    )

    private fun expense(id: String, amountMinor: Long) = Transaction(
        id = id, accountId = "account-1", type = TransactionType.EXPENSE, amountMinor = amountMinor,
        currency = "BDT", categoryId = null, payee = null, note = null, date = txDate,
        createdAt = 0L, updatedAt = 0L, isRecurring = false, deletedAt = null,
    )

    @Before
    fun setUp() = runTest {
        budgetRepository = FakeBudgetRepository()
        transactionRepository = FakeTransactionRepository()
        splitRepository = FakeTransactionSplitRepository()
        notificationRepository = FakeNotificationRepository()
        budgetRepository.create(budget, emptyList())

        val createNotification = CreateNotificationUseCase(
            notificationRepository,
            FakeNotificationPoster(),
            AppForegroundTracker(),
        )
        useCase = CheckBudgetAlertsUseCase(
            budgetRepository,
            transactionRepository,
            splitRepository,
            notificationRepository,
            CalculateBudgetUsageUseCase(),
            EvaluateBudgetAlertUseCase(),
            createNotification,
        )
    }

    @Test
    fun `does nothing below 80 percent usage`() = runTest {
        transactionRepository.create(expense("tx-1", 5_000_00L))

        useCase(now = now)

        assertTrue(notificationRepository.observeAll().first().isEmpty())
    }

    @Test
    fun `fires a budget warning the first time usage crosses 80 percent`() = runTest {
        transactionRepository.create(expense("tx-1", 8_000_00L))

        useCase(now = now)

        val notifications = notificationRepository.observeAll().first()
        assertEquals(1, notifications.size)
        assertEquals(NotificationType.BUDGET_WARNING, notifications.single().type)
        assertEquals(budget.id, notifications.single().relatedEntityId)
        assertEquals("budgets/${budget.id}", notifications.single().deepLink)
    }

    @Test
    fun `does not fire a second warning on a later check within the same period`() = runTest {
        transactionRepository.create(expense("tx-1", 8_000_00L))
        useCase(now = now)

        useCase(now = now)

        assertEquals(1, notificationRepository.observeAll().first().size)
    }

    @Test
    fun `fires exceeded once usage reaches 100 percent, in addition to the earlier warning`() = runTest {
        transactionRepository.create(expense("tx-1", 8_000_00L))
        useCase(now = now)

        transactionRepository.create(expense("tx-2", 3_000_00L))
        useCase(now = now)

        val notifications = notificationRepository.observeAll().first()
        assertEquals(2, notifications.size)
        assertTrue(notifications.any { it.type == NotificationType.BUDGET_EXCEEDED })
    }

    @Test
    fun `ignores budgets whose date range does not cover now`() = runTest {
        transactionRepository.create(expense("tx-1", 9_000_00L))

        useCase(now = periodEnd + 100_000L)

        assertTrue(notificationRepository.observeAll().first().isEmpty())
    }
}
