package com.example.wallet.domain.repository

import com.example.wallet.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

/** Implemented in Phase 5. Shape per plan.md §71. */
interface TransactionRepository {
    fun observeTransactions(): Flow<List<Transaction>>
    fun observeByAccount(accountId: String): Flow<List<Transaction>>
    fun observeByDateRange(startInclusive: Long, endInclusive: Long): Flow<List<Transaction>>
    suspend fun getTransaction(id: String): Transaction?
    suspend fun create(transaction: Transaction)
    suspend fun update(transaction: Transaction)
    suspend fun delete(id: String)
}
