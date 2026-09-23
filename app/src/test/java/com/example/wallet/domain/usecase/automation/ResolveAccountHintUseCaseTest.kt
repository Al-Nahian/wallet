package com.example.wallet.domain.usecase.automation

import com.example.wallet.domain.model.Account
import com.example.wallet.domain.model.AccountType
import com.example.wallet.domain.usecase.account.FakeAccountRepository
import com.example.wallet.domain.usecase.automation.parser.AccountHint
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ResolveAccountHintUseCaseTest {

    private fun account(id: String, name: String, type: AccountType) = Account(
        id = id,
        name = name,
        type = type,
        institutionId = null,
        currency = "BDT",
        openingBalanceMinor = 0L,
        isArchived = false,
        createdAt = 0L,
        updatedAt = 0L,
    )

    @Test
    fun `CASH auto-creates a Cash account when none exists`() = runTest {
        val repository = FakeAccountRepository()
        val useCase = ResolveAccountHintUseCase(repository)

        val result = useCase(AccountHint.CASH, emptyList())

        val resolved = result as AccountResolution.Resolved
        val created = repository.getAccount(resolved.accountId)!!
        assertEquals("Cash", created.name)
        assertEquals(AccountType.CASH, created.type)
    }

    @Test
    fun `CASH resolves to the single existing cash account`() = runTest {
        val repository = FakeAccountRepository()
        val cash = account("a1", "My Cash", AccountType.CASH)
        repository.create(cash)
        val useCase = ResolveAccountHintUseCase(repository)

        val result = useCase(AccountHint.CASH, listOf(cash))

        assertEquals(AccountResolution.Resolved("a1"), result)
    }

    @Test
    fun `CASH is ambiguous when more than one cash account exists`() = runTest {
        val repository = FakeAccountRepository()
        val useCase = ResolveAccountHintUseCase(repository)
        val accounts = listOf(
            account("a1", "Cash 1", AccountType.CASH),
            account("a2", "Cash 2", AccountType.CASH),
        )

        assertEquals(AccountResolution.Ambiguous, useCase(AccountHint.CASH, accounts))
    }

    @Test
    fun `BKASH matches an existing account by name substring`() = runTest {
        val repository = FakeAccountRepository()
        val useCase = ResolveAccountHintUseCase(repository)
        val accounts = listOf(account("a1", "My bKash", AccountType.MOBILE_WALLET))

        assertEquals(AccountResolution.Resolved("a1"), useCase(AccountHint.BKASH, accounts))
    }

    @Test
    fun `BANK is ambiguous when zero bank accounts exist (never auto-created)`() = runTest {
        val repository = FakeAccountRepository()
        val useCase = ResolveAccountHintUseCase(repository)

        assertEquals(AccountResolution.Ambiguous, useCase(AccountHint.BANK, emptyList()))
    }

    @Test
    fun `BANK resolves when exactly one bank account exists`() = runTest {
        val repository = FakeAccountRepository()
        val useCase = ResolveAccountHintUseCase(repository)
        val accounts = listOf(account("a1", "My Bank", AccountType.BANK))

        assertEquals(AccountResolution.Resolved("a1"), useCase(AccountHint.BANK, accounts))
    }

    @Test
    fun `BANK is ambiguous when multiple bank accounts exist`() = runTest {
        val repository = FakeAccountRepository()
        val useCase = ResolveAccountHintUseCase(repository)
        val accounts = listOf(
            account("a1", "Bank 1", AccountType.BANK),
            account("a2", "Bank 2", AccountType.BANK),
        )

        assertEquals(AccountResolution.Ambiguous, useCase(AccountHint.BANK, accounts))
    }
}
