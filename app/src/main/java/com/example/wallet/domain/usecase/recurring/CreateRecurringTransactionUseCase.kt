package com.example.wallet.domain.usecase.recurring

import com.example.wallet.core.common.newId
import com.example.wallet.core.common.parseMoneyToMinorUnits
import com.example.wallet.domain.model.RecurringFrequency
import com.example.wallet.domain.model.RecurringTransaction
import com.example.wallet.domain.model.TransactionType
import com.example.wallet.domain.repository.AccountRepository
import com.example.wallet.domain.repository.RecurringTransactionRepository
import javax.inject.Inject

/** plan.md §18/plans/11-recurring-goals.md — a recurring rule for a bill, subscription or
 * regular income (rent, internet, salary...). Only EXPENSE/INCOME are valid [type]s: a transfer
 * has no single account/category shape to recur against, and [GenerateDueRecurringTransactionsUseCase]
 * has no transfer-posting path. */
class CreateRecurringTransactionUseCase @Inject constructor(
    private val recurringTransactionRepository: RecurringTransactionRepository,
    private val accountRepository: AccountRepository,
) {
    suspend operator fun invoke(
        accountId: String?,
        categoryId: String?,
        amountInput: String,
        frequency: RecurringFrequency,
        nextDate: Long,
        endDate: Long?,
        type: TransactionType,
        payee: String,
        note: String,
        autoPost: Boolean,
    ): Result<RecurringTransaction> {
        if (accountId.isNullOrBlank()) {
            return Result.failure(RecurringTransactionValidationException(RecurringTransactionError.AccountRequired))
        }
        if (type != TransactionType.EXPENSE && type != TransactionType.INCOME) {
            return Result.failure(RecurringTransactionValidationException(RecurringTransactionError.InvalidType))
        }
        val amountMinor = parseMoneyToMinorUnits(amountInput)?.takeIf { it > 0 }
            ?: return Result.failure(RecurringTransactionValidationException(RecurringTransactionError.InvalidAmount))
        if (endDate != null && endDate <= nextDate) {
            return Result.failure(RecurringTransactionValidationException(RecurringTransactionError.InvalidDateRange))
        }
        val account = accountRepository.getAccount(accountId)
            ?: return Result.failure(RecurringTransactionValidationException(RecurringTransactionError.AccountRequired))

        val recurring = RecurringTransaction(
            id = newId(),
            accountId = accountId,
            categoryId = categoryId,
            amountMinor = amountMinor,
            currency = account.currency,
            frequency = frequency,
            nextDate = nextDate,
            endDate = endDate,
            type = type,
            payee = payee.trim().takeIf { it.isNotEmpty() },
            note = note.trim().takeIf { it.isNotEmpty() },
            isActive = true,
            autoPost = autoPost,
        )
        recurringTransactionRepository.create(recurring)
        return Result.success(recurring)
    }
}
