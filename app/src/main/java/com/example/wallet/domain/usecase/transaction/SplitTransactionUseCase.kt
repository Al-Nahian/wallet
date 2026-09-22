package com.example.wallet.domain.usecase.transaction

import com.example.wallet.core.common.newId
import com.example.wallet.domain.model.TransactionSplit
import com.example.wallet.domain.repository.TransactionRepository
import com.example.wallet.domain.repository.TransactionSplitRepository
import com.example.wallet.domain.rules.validateSplitsSum
import javax.inject.Inject

data class SplitInput(val categoryId: String, val amountMinor: Long, val note: String?)

/**
 * plan.md §13/§26 rule 3: replaces every split row for a transaction, atomically, after
 * validating they sum exactly to the parent transaction's amount. An empty [splits] list always
 * fails the sum check (since every valid transaction amount is non-zero), so no separate
 * emptiness check is needed.
 */
class SplitTransactionUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val splitRepository: TransactionSplitRepository,
) {
    suspend operator fun invoke(transactionId: String, splits: List<SplitInput>): Result<Unit> {
        val transaction = transactionRepository.getTransaction(transactionId)
            ?: return Result.failure(TransactionValidationException(TransactionError.TransactionNotFound))

        if (!validateSplitsSum(transaction.amountMinor, splits.map { it.amountMinor })) {
            return Result.failure(TransactionValidationException(TransactionError.SplitSumMismatch))
        }

        val domainSplits = splits.map {
            TransactionSplit(
                id = newId(),
                transactionId = transactionId,
                categoryId = it.categoryId,
                amountMinor = it.amountMinor,
                note = it.note,
            )
        }
        splitRepository.replaceSplits(transactionId, domainSplits)
        return Result.success(Unit)
    }
}
