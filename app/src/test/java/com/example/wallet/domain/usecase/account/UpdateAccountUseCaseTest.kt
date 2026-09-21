package com.example.wallet.domain.usecase.account

import com.example.wallet.domain.model.AccountType
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UpdateAccountUseCaseTest {

    private lateinit var accountRepository: FakeAccountRepository
    private lateinit var createAccountUseCase: CreateAccountUseCase
    private lateinit var updateAccountUseCase: UpdateAccountUseCase

    @Before
    fun setUp() {
        accountRepository = FakeAccountRepository()
        val institutionRepository = FakeInstitutionRepository()
        createAccountUseCase = CreateAccountUseCase(accountRepository, institutionRepository)
        updateAccountUseCase = UpdateAccountUseCase(accountRepository, institutionRepository)
    }

    @Test
    fun `updates name type and institution, leaves opening balance untouched`() = runTest {
        val original = createAccountUseCase("Cash", AccountType.CASH, null, "BDT", "500").getOrThrow()

        val result = updateAccountUseCase(original.id, "Wallet Cash", AccountType.MOBILE_WALLET, "bKash")

        assertTrue(result.isSuccess)
        val updated = result.getOrThrow()
        assertEquals("Wallet Cash", updated.name)
        assertEquals(AccountType.MOBILE_WALLET, updated.type)
        assertEquals(50000L, updated.openingBalanceMinor)
    }

    @Test
    fun `updating an unknown account fails with AccountNotFound`() = runTest {
        val result = updateAccountUseCase("missing", "Name", AccountType.CASH, null)

        assertTrue(result.isFailure)
        assertEquals(
            AccountError.AccountNotFound,
            (result.exceptionOrNull() as AccountValidationException).error,
        )
    }

    @Test
    fun `blank name is rejected`() = runTest {
        val original = createAccountUseCase("Cash", AccountType.CASH, null, "BDT", "0").getOrThrow()

        val result = updateAccountUseCase(original.id, "   ", AccountType.CASH, null)

        assertTrue(result.isFailure)
        assertEquals(
            AccountError.NameRequired,
            (result.exceptionOrNull() as AccountValidationException).error,
        )
    }
}
