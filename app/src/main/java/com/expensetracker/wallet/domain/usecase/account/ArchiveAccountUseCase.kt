package com.expensetracker.wallet.domain.usecase.account

import com.expensetracker.wallet.domain.repository.AccountRepository
import javax.inject.Inject

/** plan.md §11/§70 — soft archive, never a physical delete. */
class ArchiveAccountUseCase @Inject constructor(
    private val accountRepository: AccountRepository,
) {
    suspend operator fun invoke(accountId: String): Result<Unit> {
        val existing = accountRepository.getAccount(accountId)
            ?: return Result.failure(AccountValidationException(AccountError.AccountNotFound))

        accountRepository.update(existing.copy(isArchived = true, updatedAt = System.currentTimeMillis()))
        return Result.success(Unit)
    }
}
