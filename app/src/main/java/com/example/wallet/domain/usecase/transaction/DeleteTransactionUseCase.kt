package com.example.wallet.domain.usecase.transaction

import com.example.wallet.domain.repository.TransactionRepository
import javax.inject.Inject

/** plan.md §44 — soft delete only, never a physical delete. */
class DeleteTransactionUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
) {
    suspend operator fun invoke(transactionId: String): Result<Unit> {
        transactionRepository.getTransaction(transactionId)
            ?: return Result.failure(TransactionValidationException(TransactionError.TransactionNotFound))

        transactionRepository.delete(transactionId)
        return Result.success(Unit)
    }
}
