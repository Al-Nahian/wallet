package com.example.wallet.domain.usecase.account

import com.example.wallet.core.common.newId
import com.example.wallet.core.common.parseMoneyToMinorUnits
import com.example.wallet.domain.model.Account
import com.example.wallet.domain.model.AccountType
import com.example.wallet.domain.repository.AccountRepository
import com.example.wallet.domain.repository.InstitutionRepository
import javax.inject.Inject

/** plan.md §70. Validates then persists a new account (§21/§56 — friendly errors, never a crash). */
class CreateAccountUseCase @Inject constructor(
    private val accountRepository: AccountRepository,
    private val institutionRepository: InstitutionRepository,
) {
    suspend operator fun invoke(
        name: String,
        type: AccountType,
        institutionName: String?,
        currency: String,
        openingBalanceInput: String,
    ): Result<Account> {
        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) {
            return Result.failure(AccountValidationException(AccountError.NameRequired))
        }
        if (currency.isBlank()) {
            return Result.failure(AccountValidationException(AccountError.CurrencyRequired))
        }
        val openingBalanceMinor = parseMoneyToMinorUnits(openingBalanceInput)
            ?: return Result.failure(AccountValidationException(AccountError.InvalidOpeningBalance))

        val institutionId = institutionName
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?.let { institutionRepository.findOrCreateByName(it).id }

        val now = System.currentTimeMillis()
        val account = Account(
            id = newId(),
            name = trimmedName,
            type = type,
            institutionId = institutionId,
            currency = currency.trim().uppercase(),
            openingBalanceMinor = openingBalanceMinor,
            isArchived = false,
            createdAt = now,
            updatedAt = now,
        )

        accountRepository.create(account)
        return Result.success(account)
    }
}
