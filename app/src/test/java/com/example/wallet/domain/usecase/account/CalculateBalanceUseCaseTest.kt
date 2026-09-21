package com.example.wallet.domain.usecase.account

import com.example.wallet.domain.model.Account
import com.example.wallet.domain.model.AccountType
import org.junit.Assert.assertEquals
import org.junit.Test

class CalculateBalanceUseCaseTest {

    private val useCase = CalculateBalanceUseCase()

    @Test
    fun `balance equals opening balance when no transactions exist`() {
        val account = Account(
            id = "account-1",
            name = "Cash",
            type = AccountType.CASH,
            institutionId = null,
            currency = "BDT",
            openingBalanceMinor = 100_000_00,
            isArchived = false,
            createdAt = 0L,
            updatedAt = 0L,
        )

        assertEquals(100_000_00L, useCase(account))
    }
}
