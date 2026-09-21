package com.example.wallet.domain.repository

import com.example.wallet.domain.model.RecurringTransaction
import kotlinx.coroutines.flow.Flow

/** Implemented in Phase 11. */
interface RecurringTransactionRepository {
    fun observeActive(): Flow<List<RecurringTransaction>>
    suspend fun getDue(beforeOrAt: Long): List<RecurringTransaction>
    suspend fun create(recurring: RecurringTransaction)
    suspend fun update(recurring: RecurringTransaction)
}
