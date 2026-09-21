package com.example.wallet.domain.usecase.transaction

import com.example.wallet.core.common.newId
import com.example.wallet.core.common.parseMoneyToMinorUnits
import com.example.wallet.domain.model.Transaction
import com.example.wallet.domain.model.TransactionType
import com.example.wallet.domain.repository.AccountRepository
import com.example.wallet.domain.repository.TransactionRepository
import javax.inject.Inject

/**
 * plan.md §21/§70. Only `EXPENSE`/`INCOME` are meaningful here — `TRANSFER` is Phase 6's
 * `CreateTransferUseCase`, not this one (§26 rule: transfers are never income or expense).
 * Currency is always the account's own currency; no conversion exists yet (§27 is later work).
 */
class CreateTransactionUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository,
) {
    suspend operator fun invoke(
        type: TransactionType,
        accountId: String,
        amountInput: String,
        categoryId: String?,
        payee: String?,
        note: String?,
        date: Long,
    ): Result<Transaction> {
        val account = accountRepository.getAccount(accountId)
            ?: return Result.failure(TransactionValidationException(TransactionError.AccountNotFound))
        if (account.isArchived) {
            return Result.failure(TransactionValidationException(TransactionError.AccountArchived))
        }

        val amountMinor = parseMoneyToMinorUnits(amountInput)?.takeIf { it > 0 }
            ?: return Result.failure(TransactionValidationException(TransactionError.InvalidAmount))

        val now = System.currentTimeMillis()
        val transaction = Transaction(
            id = newId(),
            accountId = accountId,
            type = type,
            amountMinor = amountMinor,
            currency = account.currency,
            categoryId = categoryId,
            payee = payee?.trim()?.takeIf { it.isNotEmpty() },
            note = note?.trim()?.takeIf { it.isNotEmpty() },
            date = date,
            createdAt = now,
            updatedAt = now,
            isRecurring = false,
            deletedAt = null,
        )

        transactionRepository.create(transaction)
        return Result.success(transaction)
    }
}
