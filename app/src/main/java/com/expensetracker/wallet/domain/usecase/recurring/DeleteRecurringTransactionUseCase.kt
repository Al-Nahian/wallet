package com.expensetracker.wallet.domain.usecase.recurring

import com.expensetracker.wallet.domain.repository.RecurringTransactionRepository
import javax.inject.Inject

/** Deletes the rule itself only — any transactions it already auto-posted keep their
 * `recurringTransactionId` pointing at a now-gone rule, same as [TransactionEntity.categoryId]
 * surviving a deleted category (plan.md §15's "never silently cascade" policy). */
class DeleteRecurringTransactionUseCase @Inject constructor(
    private val recurringTransactionRepository: RecurringTransactionRepository,
) {
    suspend operator fun invoke(id: String) {
        recurringTransactionRepository.delete(id)
    }
}
