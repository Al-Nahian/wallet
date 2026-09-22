package com.example.wallet.data.repository

import androidx.room.withTransaction
import com.example.wallet.core.database.AppDatabase
import com.example.wallet.data.local.dao.TransactionDao
import com.example.wallet.domain.model.Transaction
import com.example.wallet.domain.model.TransactionType
import com.example.wallet.domain.repository.TransactionRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Every write goes through `AppDatabase.withTransaction` (plan.md §72), even for today's
 * single-row inserts/updates — establishing the pattern now so Phase 6's multi-row transfers
 * (two linked transaction rows, atomically) have a proven wrapper to build on.
 */
class TransactionRepositoryImpl @Inject constructor(
    private val appDatabase: AppDatabase,
    private val transactionDao: TransactionDao,
) : TransactionRepository {

    override fun observeTransactions(): Flow<List<Transaction>> =
        transactionDao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override fun observeByAccount(accountId: String): Flow<List<Transaction>> =
        transactionDao.observeByAccount(accountId).map { entities -> entities.map { it.toDomain() } }

    override fun observeByDateRange(startInclusive: Long, endInclusive: Long): Flow<List<Transaction>> =
        transactionDao.observeByDateRange(startInclusive, endInclusive)
            .map { entities -> entities.map { it.toDomain() } }

    override suspend fun getTransaction(id: String): Transaction? = transactionDao.getById(id)?.toDomain()

    override suspend fun create(transaction: Transaction) {
        appDatabase.withTransaction { transactionDao.insert(transaction.toEntity()) }
    }

    override suspend fun update(transaction: Transaction) {
        appDatabase.withTransaction { transactionDao.update(transaction.toEntity()) }
    }

    override suspend fun delete(id: String) {
        appDatabase.withTransaction { transactionDao.softDelete(id, System.currentTimeMillis()) }
    }

    override suspend fun sumByAccountAndType(accountId: String, type: TransactionType): Long =
        transactionDao.sumByAccountAndType(accountId, type)

    override suspend fun sumByTypeInRange(type: TransactionType, startInclusive: Long, endInclusive: Long): Long =
        transactionDao.sumByTypeInRange(type, startInclusive, endInclusive)

    override suspend fun createTransferPair(outgoing: Transaction, incoming: Transaction) {
        appDatabase.withTransaction {
            transactionDao.insert(outgoing.toEntity())
            transactionDao.insert(incoming.toEntity())
        }
    }

    override suspend fun deleteTransferPair(transactionIds: List<String>) {
        val deletedAt = System.currentTimeMillis()
        appDatabase.withTransaction {
            transactionIds.forEach { id -> transactionDao.softDelete(id, deletedAt) }
        }
    }

    override suspend fun getByTransferId(transferId: String): List<Transaction> =
        transactionDao.getByTransferId(transferId).map { it.toDomain() }
}
