package com.example.wallet.domain.usecase.dashboard

import org.junit.Assert.assertEquals
import org.junit.Test

class GetSavingsRateUseCaseTest {

    private val useCase = GetSavingsRateUseCase()

    @Test
    fun `savings rate is savings as a percentage of income`() {
        assertEquals(60.0, useCase(savingsMinor = 30_000_00L, incomeMinor = 50_000_00L), 0.001)
    }

    @Test
    fun `zero income never divides by zero`() {
        assertEquals(0.0, useCase(savingsMinor = 0L, incomeMinor = 0L), 0.001)
    }

    @Test
    fun `negative savings gives a negative rate`() {
        assertEquals(-50.0, useCase(savingsMinor = -5_000_00L, incomeMinor = 10_000_00L), 0.001)
    }
}
