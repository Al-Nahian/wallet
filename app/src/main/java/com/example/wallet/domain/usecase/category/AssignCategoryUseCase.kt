package com.example.wallet.domain.usecase.category

import com.example.wallet.domain.repository.TransactionRepository
import com.example.wallet.domain.usecase.transaction.TransactionError
import com.example.wallet.domain.usecase.transaction.TransactionValidationException
import javax.inject.Inject

/** plan.md §70 — re-categorizes an existing transaction without going through the full
 * add/edit form (e.g. a quick "recategorize" action from the transaction list). */
class AssignCategoryUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
) {
    suspend operator fun invoke(transactionId: String, categoryId: String?): Result<Unit> {
        val transaction = transactionRepository.getTransaction(transactionId)
            ?: return Result.failure(TransactionValidationException(TransactionError.TransactionNotFound))

        transactionRepository.update(transaction.copy(categoryId = categoryId, updatedAt = System.currentTimeMillis()))
        return Result.success(Unit)
    }
}
