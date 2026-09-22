package com.example.wallet.domain.repository

import com.example.wallet.domain.model.TransactionSplit
import kotlinx.coroutines.flow.Flow

/** plan.md §13. Implemented in Phase 6. */
interface TransactionSplitRepository {
    fun observeByTransaction(transactionId: String): Flow<List<TransactionSplit>>
    fun observeAllSplits(): Flow<List<TransactionSplit>>

    /** Replaces every split row for [transactionId] atomically (delete-then-insert). */
    suspend fun replaceSplits(transactionId: String, splits: List<TransactionSplit>)
}
