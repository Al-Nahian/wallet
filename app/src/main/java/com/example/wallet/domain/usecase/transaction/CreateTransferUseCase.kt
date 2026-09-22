package com.example.wallet.domain.usecase.transaction

import com.example.wallet.core.common.newId
import com.example.wallet.core.common.parseMoneyToMinorUnits
import com.example.wallet.domain.model.Transaction
import com.example.wallet.domain.model.TransactionType
import com.example.wallet.domain.repository.AccountRepository
import com.example.wallet.domain.repository.TransactionRepository
import javax.inject.Inject

/**
 * plan.md §22/§72: creates one linked pair of `TRANSFER`-type transaction rows — an outgoing
 * leg on [fromAccountId] and an incoming leg on [toAccountId] — atomically via
 * [TransactionRepository.createTransferPair]. Both legs share the same [Transaction.transferId]
 * and the same magnitude; the outgoing leg's `amountMinor` is stored negated so
 * `CalculateBalanceUseCase` can net a transfer straight from `SUM(amountMinor)` per account
 * without a separate direction column. Transfers are never counted as income/expense (§26 rule
 * 1) because their `type` is `TRANSFER`, not `EXPENSE`/`INCOME`.
 */
class CreateTransferUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository,
) {
    suspend operator fun invoke(
        fromAccountId: String,
        toAccountId: String,
        amountInput: String,
        note: String?,
        date: Long,
    ): Result<Unit> {
        if (fromAccountId == toAccountId) {
            return Result.failure(TransactionValidationException(TransactionError.SameAccountTransfer))
        }

        val fromAccount = accountRepository.getAccount(fromAccountId)
            ?: return Result.failure(TransactionValidationException(TransactionError.AccountNotFound))
        if (fromAccount.isArchived) {
            return Result.failure(TransactionValidationException(TransactionError.AccountArchived))
        }

        val toAccount = accountRepository.getAccount(toAccountId)
            ?: return Result.failure(TransactionValidationException(TransactionError.DestinationAccountNotFound))
        if (toAccount.isArchived) {
            return Result.failure(TransactionValidationException(TransactionError.DestinationAccountArchived))
        }

        val amountMinor = parseMoneyToMinorUnits(amountInput)?.takeIf { it > 0 }
            ?: return Result.failure(TransactionValidationException(TransactionError.InvalidAmount))

        val now = System.currentTimeMillis()
        val transferId = newId()
        val cleanNote = note?.trim()?.takeIf { it.isNotEmpty() }

        val outgoing = Transaction(
            id = newId(),
            accountId = fromAccountId,
            type = TransactionType.TRANSFER,
            amountMinor = -amountMinor,
            currency = fromAccount.currency,
            categoryId = null,
            payee = null,
            note = cleanNote,
            date = date,
            createdAt = now,
            updatedAt = now,
            isRecurring = false,
            deletedAt = null,
            transferId = transferId,
        )
        val incoming = outgoing.copy(
            id = newId(),
            accountId = toAccountId,
            amountMinor = amountMinor,
            currency = toAccount.currency,
        )

        transactionRepository.createTransferPair(outgoing, incoming)
        return Result.success(Unit)
    }
}
