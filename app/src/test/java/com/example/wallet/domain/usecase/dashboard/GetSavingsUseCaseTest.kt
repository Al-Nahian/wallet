package com.example.wallet.domain.usecase.dashboard

import org.junit.Assert.assertEquals
import org.junit.Test

class GetSavingsUseCaseTest {

    private val useCase = GetSavingsUseCase()

    @Test
    fun `savings is income minus expenses`() {
        assertEquals(30_000_00L, useCase(incomeMinor = 50_000_00L, expenseMinor = 20_000_00L))
    }

    @Test
    fun `savings can be negative when spending exceeds income`() {
        assertEquals(-10_000_00L, useCase(incomeMinor = 10_000_00L, expenseMinor = 20_000_00L))
    }
}
