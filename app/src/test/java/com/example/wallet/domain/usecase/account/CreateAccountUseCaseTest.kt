package com.example.wallet.domain.usecase.account

import com.example.wallet.domain.model.AccountType
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CreateAccountUseCaseTest {

    private lateinit var accountRepository: FakeAccountRepository
    private lateinit var institutionRepository: FakeInstitutionRepository
    private lateinit var useCase: CreateAccountUseCase

    @Before
    fun setUp() {
        accountRepository = FakeAccountRepository()
        institutionRepository = FakeInstitutionRepository()
        useCase = CreateAccountUseCase(accountRepository, institutionRepository)
    }

    @Test
    fun `valid input creates and persists the account`() = runTest {
        val result = useCase(
            name = "Emergency Fund",
            type = AccountType.SAVINGS,
            institutionName = "BRAC Bank",
            currency = "BDT",
            openingBalanceInput = "100000.50",
        )

        assertTrue(result.isSuccess)
        val account = result.getOrThrow()
        assertEquals("Emergency Fund", account.name)
        assertEquals(AccountType.SAVINGS, account.type)
        assertEquals("BDT", account.currency)
        assertEquals(100_000_50L, account.openingBalanceMinor)
        assertEquals(account, accountRepository.getAccount(account.id))
    }

    @Test
    fun `blank name is rejected`() = runTest {
        val result = useCase(
            name = "   ",
            type = AccountType.CASH,
            institutionName = null,
            currency = "BDT",
            openingBalanceInput = "0",
        )

        assertTrue(result.isFailure)
        val error = (result.exceptionOrNull() as AccountValidationException).error
        assertEquals(AccountError.NameRequired, error)
    }

    @Test
    fun `invalid opening balance is rejected`() = runTest {
        val result = useCase(
            name = "Wallet",
            type = AccountType.CASH,
            institutionName = null,
            currency = "BDT",
            openingBalanceInput = "not-a-number",
        )

        assertTrue(result.isFailure)
        val error = (result.exceptionOrNull() as AccountValidationException).error
        assertEquals(AccountError.InvalidOpeningBalance, error)
    }

    @Test
    fun `blank institution name does not create an institution`() = runTest {
        val result = useCase(
            name = "Cash",
            type = AccountType.CASH,
            institutionName = "  ",
            currency = "BDT",
            openingBalanceInput = "500",
        )

        assertTrue(result.isSuccess)
        assertEquals(null, result.getOrThrow().institutionId)
    }

    @Test
    fun `same institution name reuses the same institution id`() = runTest {
        val first = useCase("A", AccountType.BANK, "BRAC Bank", "BDT", "0").getOrThrow()
        val second = useCase("B", AccountType.BANK, "BRAC Bank", "BDT", "0").getOrThrow()

        assertEquals(first.institutionId, second.institutionId)
    }
}
