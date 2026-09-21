package com.example.wallet.domain.usecase.account

import com.example.wallet.domain.model.Account
import com.example.wallet.domain.model.AccountType
import com.example.wallet.domain.repository.AccountRepository
import com.example.wallet.domain.repository.InstitutionRepository
import javax.inject.Inject

/**
 * plan.md §70. Only name/type/institution are editable per §67's acceptance criteria — currency
 * and opening balance are fixed at creation (changing either after transactions may exist against
 * the account would silently distort the ledger, so that's deliberately not offered here).
 */
class UpdateAccountUseCase @Inject constructor(
    private val accountRepository: AccountRepository,
    private val institutionRepository: InstitutionRepository,
) {
    suspend operator fun invoke(
        accountId: String,
        name: String,
        type: AccountType,
        institutionName: String?,
    ): Result<Account> {
        val existing = accountRepository.getAccount(accountId)
            ?: return Result.failure(AccountValidationException(AccountError.AccountNotFound))

        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) {
            return Result.failure(AccountValidationException(AccountError.NameRequired))
        }

        val institutionId = institutionName
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?.let { institutionRepository.findOrCreateByName(it).id }

        val updated = existing.copy(
            name = trimmedName,
            type = type,
            institutionId = institutionId,
            updatedAt = System.currentTimeMillis(),
        )

        accountRepository.update(updated)
        return Result.success(updated)
    }
}
