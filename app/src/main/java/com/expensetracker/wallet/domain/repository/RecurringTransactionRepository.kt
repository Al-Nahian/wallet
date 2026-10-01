package com.expensetracker.wallet.domain.repository

import com.expensetracker.wallet.domain.model.RecurringTransaction
import kotlinx.coroutines.flow.Flow

interface RecurringTransactionRepository {
    fun observeActive(): Flow<List<RecurringTransaction>>
    fun observeById(id: String): Flow<RecurringTransaction?>
    suspend fun getById(id: String): RecurringTransaction?
    suspend fun getDue(beforeOrAt: Long): List<RecurringTransaction>
    suspend fun create(recurring: RecurringTransaction)
    suspend fun update(recurring: RecurringTransaction)
    suspend fun delete(id: String)
}
