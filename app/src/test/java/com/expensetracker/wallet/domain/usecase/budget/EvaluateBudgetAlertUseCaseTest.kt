package com.expensetracker.wallet.domain.usecase.budget

import com.expensetracker.wallet.domain.model.NotificationType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class EvaluateBudgetAlertUseCaseTest {

    private val useCase = EvaluateBudgetAlertUseCase()

    @Test
    fun `below 80 percent fires nothing`() {
        assertNull(useCase(79.9, emptySet()))
    }

    @Test
    fun `at or above 80 percent fires a warning the first time`() {
        assertEquals(NotificationType.BUDGET_WARNING, useCase(80.0, emptySet()))
    }

    @Test
    fun `does not re-fire a warning already sent this period`() {
        assertNull(useCase(85.0, setOf(NotificationType.BUDGET_WARNING)))
    }

    @Test
    fun `at or above 100 percent fires exceeded even if warning already fired`() {
        assertEquals(NotificationType.BUDGET_EXCEEDED, useCase(100.0, setOf(NotificationType.BUDGET_WARNING)))
    }

    @Test
    fun `does not re-fire exceeded already sent this period`() {
        assertNull(useCase(150.0, setOf(NotificationType.BUDGET_WARNING, NotificationType.BUDGET_EXCEEDED)))
    }
}
