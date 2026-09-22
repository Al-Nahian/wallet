package com.example.wallet.domain.usecase.transaction

import com.example.wallet.domain.model.TransactionSplit
import com.example.wallet.domain.repository.TransactionSplitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/** In-memory fake mirroring the real DAO's delete-then-insert replace semantics. */
class FakeTransactionSplitRepository : TransactionSplitRepository {
    private val splits = MutableStateFlow<Map<String, TransactionSplit>>(emptyMap())

    override fun observeByTransaction(transactionId: String): Flow<List<TransactionSplit>> =
        splits.map { it.values.filter { split -> split.transactionId == transactionId } }

    override fun observeAllSplits(): Flow<List<TransactionSplit>> = splits.map { it.values.toList() }

    override suspend fun replaceSplits(transactionId: String, splits: List<TransactionSplit>) {
        val remaining = this.splits.value.filterValues { it.transactionId != transactionId }
        this.splits.value = remaining + splits.associateBy { it.id }
    }
}
