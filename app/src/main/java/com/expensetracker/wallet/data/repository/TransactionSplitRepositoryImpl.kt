package com.expensetracker.wallet.data.repository

import androidx.room.withTransaction
import com.expensetracker.wallet.core.database.AppDatabase
import com.expensetracker.wallet.data.local.dao.TransactionSplitDao
import com.expensetracker.wallet.domain.model.TransactionSplit
import com.expensetracker.wallet.domain.repository.TransactionSplitRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TransactionSplitRepositoryImpl @Inject constructor(
    private val appDatabase: AppDatabase,
    private val transactionSplitDao: TransactionSplitDao,
) : TransactionSplitRepository {

    override fun observeByTransaction(transactionId: String): Flow<List<TransactionSplit>> =
        transactionSplitDao.observeByTransaction(transactionId).map { entities -> entities.map { it.toDomain() } }

    override fun observeAllSplits(): Flow<List<TransactionSplit>> =
        transactionSplitDao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override suspend fun replaceSplits(transactionId: String, splits: List<TransactionSplit>) {
        appDatabase.withTransaction {
            transactionSplitDao.deleteByTransaction(transactionId)
            transactionSplitDao.upsertAll(splits.map { it.toEntity() })
        }
    }
}
