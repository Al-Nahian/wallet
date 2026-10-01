package com.expensetracker.wallet.domain.usecase.recurring

import com.expensetracker.wallet.core.common.parseMoneyToMinorUnits
import com.expensetracker.wallet.domain.model.RecurringFrequency
import com.expensetracker.wallet.domain.model.RecurringTransaction
import com.expensetracker.wallet.domain.model.TransactionType
import com.expensetracker.wallet.domain.repository.RecurringTransactionRepository
import javax.inject.Inject

class UpdateRecurringTransactionUseCase @Inject constructor(
    private val recurringTransactionRepository: RecurringTransactionRepository,
) {
    suspend operator fun invoke(
        existing: RecurringTransaction,
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
        isActive: Boolean,
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

        val updated = existing.copy(
            accountId = accountId,
            categoryId = categoryId,
            amountMinor = amountMinor,
            frequency = frequency,
            nextDate = nextDate,
            endDate = endDate,
            type = type,
            payee = payee.trim().takeIf { it.isNotEmpty() },
            note = note.trim().takeIf { it.isNotEmpty() },
            autoPost = autoPost,
            isActive = isActive,
        )
        recurringTransactionRepository.update(updated)
        return Result.success(updated)
    }
}
