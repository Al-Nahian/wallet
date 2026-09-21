package com.example.wallet.domain.usecase.transaction

import com.example.wallet.core.common.parseMoneyToMinorUnits
import com.example.wallet.domain.model.Transaction
import com.example.wallet.domain.model.TransactionType
import com.example.wallet.domain.repository.AccountRepository
import com.example.wallet.domain.repository.TransactionRepository
import javax.inject.Inject

class UpdateTransactionUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository,
) {
    suspend operator fun invoke(
        transactionId: String,
        type: TransactionType,
        accountId: String,
        amountInput: String,
        categoryId: String?,
        payee: String?,
        note: String?,
        date: Long,
    ): Result<Transaction> {
        val existing = transactionRepository.getTransaction(transactionId)
            ?: return Result.failure(TransactionValidationException(TransactionError.TransactionNotFound))

        val account = accountRepository.getAccount(accountId)
            ?: return Result.failure(TransactionValidationException(TransactionError.AccountNotFound))
        if (account.isArchived) {
            return Result.failure(TransactionValidationException(TransactionError.AccountArchived))
        }

        val amountMinor = parseMoneyToMinorUnits(amountInput)?.takeIf { it > 0 }
            ?: return Result.failure(TransactionValidationException(TransactionError.InvalidAmount))

        val updated = existing.copy(
            type = type,
            accountId = accountId,
            amountMinor = amountMinor,
            currency = account.currency,
            categoryId = categoryId,
            payee = payee?.trim()?.takeIf { it.isNotEmpty() },
            note = note?.trim()?.takeIf { it.isNotEmpty() },
            date = date,
            updatedAt = System.currentTimeMillis(),
        )

        transactionRepository.update(updated)
        return Result.success(updated)
    }
}
