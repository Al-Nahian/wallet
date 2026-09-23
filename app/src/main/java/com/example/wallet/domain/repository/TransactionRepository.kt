package com.example.wallet.domain.repository

import com.example.wallet.domain.model.Transaction
import com.example.wallet.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow

/** Implemented in Phase 5. Shape per plan.md §71. */
interface TransactionRepository {
    fun observeTransactions(): Flow<List<Transaction>>
    fun observeByAccount(accountId: String): Flow<List<Transaction>>
    fun observeByDateRange(startInclusive: Long, endInclusive: Long): Flow<List<Transaction>>
    suspend fun getTransaction(id: String): Transaction?

    /** plans/14-sms-notification-automation.md — the dedup check before the capture pipeline
     * ever creates a second transaction for the same real-world SMS/notification event. */
    suspend fun findBySourceReference(sourceReference: String): Transaction?
    suspend fun create(transaction: Transaction)

    /** plans/12-import-export.md — commits an entire CSV import as one atomic batch. */
    suspend fun createBatch(transactions: List<Transaction>)
    suspend fun update(transaction: Transaction)
    suspend fun delete(id: String)

    /** Database-aggregated sums (§54 — never sum a fully-loaded list in Kotlin). */
    suspend fun sumByAccountAndType(accountId: String, type: TransactionType): Long
    suspend fun sumByTypeInRange(type: TransactionType, startInclusive: Long, endInclusive: Long): Long
    suspend fun sumByAccountAndTypeInRange(
        accountId: String,
        type: TransactionType,
        startInclusive: Long,
        endInclusive: Long,
    ): Long

    /** plan.md §22/§72 — the two legs of a transfer, created or soft-deleted atomically. */
    suspend fun createTransferPair(outgoing: Transaction, incoming: Transaction)
    suspend fun deleteTransferPair(transactionIds: List<String>)
    suspend fun getByTransferId(transferId: String): List<Transaction>
}
