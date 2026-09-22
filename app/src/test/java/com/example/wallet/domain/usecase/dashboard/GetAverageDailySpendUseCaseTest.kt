package com.example.wallet.domain.usecase.dashboard

import org.junit.Assert.assertEquals
import org.junit.Test

class GetAverageDailySpendUseCaseTest {

    private val useCase = GetAverageDailySpendUseCase()

    @Test
    fun `spreads monthly expense evenly across days elapsed`() {
        assertEquals(1_000_00L, useCase(monthlyExpenseMinor = 10_000_00L, dayOfMonth = 10))
    }

    @Test
    fun `zero days never divides by zero`() {
        assertEquals(0L, useCase(monthlyExpenseMinor = 10_000_00L, dayOfMonth = 0))
    }
}
