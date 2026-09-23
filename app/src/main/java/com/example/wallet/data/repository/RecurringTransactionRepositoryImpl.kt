package com.example.wallet.data.repository

import androidx.room.withTransaction
import com.example.wallet.core.database.AppDatabase
import com.example.wallet.data.local.dao.RecurringTransactionDao
import com.example.wallet.domain.model.RecurringTransaction
import com.example.wallet.domain.repository.RecurringTransactionRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RecurringTransactionRepositoryImpl @Inject constructor(
    private val appDatabase: AppDatabase,
    private val recurringTransactionDao: RecurringTransactionDao,
) : RecurringTransactionRepository {

    override fun observeActive(): Flow<List<RecurringTransaction>> =
        recurringTransactionDao.observeActive().map { entities -> entities.map { it.toDomain() } }

    override fun observeById(id: String): Flow<RecurringTransaction?> =
        recurringTransactionDao.observeById(id).map { it?.toDomain() }

    override suspend fun getById(id: String): RecurringTransaction? =
        recurringTransactionDao.getById(id)?.toDomain()

    override suspend fun getDue(beforeOrAt: Long): List<RecurringTransaction> =
        recurringTransactionDao.getDue(beforeOrAt).map { it.toDomain() }

    override suspend fun create(recurring: RecurringTransaction) {
        appDatabase.withTransaction { recurringTransactionDao.insert(recurring.toEntity()) }
    }

    override suspend fun update(recurring: RecurringTransaction) {
        appDatabase.withTransaction { recurringTransactionDao.update(recurring.toEntity()) }
    }

    override suspend fun delete(id: String) {
        appDatabase.withTransaction { recurringTransactionDao.delete(id) }
    }
}
