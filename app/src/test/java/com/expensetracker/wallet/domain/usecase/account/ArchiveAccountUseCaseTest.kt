package com.expensetracker.wallet.domain.usecase.account

import com.expensetracker.wallet.domain.model.AccountType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ArchiveAccountUseCaseTest {

    private lateinit var accountRepository: FakeAccountRepository
    private lateinit var createAccountUseCase: CreateAccountUseCase
    private lateinit var archiveAccountUseCase: ArchiveAccountUseCase

    @Before
    fun setUp() {
        accountRepository = FakeAccountRepository()
        val institutionRepository = FakeInstitutionRepository()
        createAccountUseCase = CreateAccountUseCase(accountRepository, institutionRepository)
        archiveAccountUseCase = ArchiveAccountUseCase(accountRepository)
    }

    @Test
    fun `archived account is excluded from observeActiveAccounts but still queryable directly`() = runTest {
        val account = createAccountUseCase("Cash", AccountType.CASH, null, "BDT", "0").getOrThrow()

        val result = archiveAccountUseCase(account.id)

        assertTrue(result.isSuccess)
        assertTrue(accountRepository.observeActiveAccounts().first().isEmpty())
        val archived = accountRepository.getAccount(account.id)
        assertEquals(true, archived?.isArchived)
    }

    @Test
    fun `archiving an unknown account fails with AccountNotFound`() = runTest {
        val result = archiveAccountUseCase("does-not-exist")

        assertTrue(result.isFailure)
        val error = (result.exceptionOrNull() as AccountValidationException).error
        assertEquals(AccountError.AccountNotFound, error)
    }
}
