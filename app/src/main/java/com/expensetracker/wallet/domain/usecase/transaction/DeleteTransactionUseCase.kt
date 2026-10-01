package com.expensetracker.wallet.domain.usecase.transaction

import com.expensetracker.wallet.domain.repository.TransactionRepository
import javax.inject.Inject

/**
 * plan.md §44 — soft delete only, never a physical delete. Deleting one leg of a transfer
 * (§22/§72) soft-deletes both legs atomically, so a transfer never ends up half-deleted.
 */
class DeleteTransactionUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
) {
    suspend operator fun invoke(transactionId: String): Result<Unit> {
        val existing = transactionRepository.getTransaction(transactionId)
            ?: return Result.failure(TransactionValidationException(TransactionError.TransactionNotFound))

        val transferId = existing.transferId
        if (transferId != null) {
            val legs = transactionRepository.getByTransferId(transferId)
            val ids = legs.map { it.id }.ifEmpty { listOf(transactionId) }
            transactionRepository.deleteTransferPair(ids)
        } else {
            transactionRepository.delete(transactionId)
        }
        return Result.success(Unit)
    }
}
