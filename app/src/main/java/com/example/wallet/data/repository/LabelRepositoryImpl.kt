package com.example.wallet.data.repository

import androidx.room.withTransaction
import com.example.wallet.core.database.AppDatabase
import com.example.wallet.data.local.dao.LabelDao
import com.example.wallet.data.local.dao.TransactionLabelDao
import com.example.wallet.data.local.entity.TransactionLabelEntity
import com.example.wallet.domain.model.Label
import com.example.wallet.domain.repository.LabelRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class LabelRepositoryImpl @Inject constructor(
    private val appDatabase: AppDatabase,
    private val labelDao: LabelDao,
    private val transactionLabelDao: TransactionLabelDao,
) : LabelRepository {

    override fun observeLabels(): Flow<List<Label>> =
        labelDao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override fun observeLabelsForTransaction(transactionId: String): Flow<List<Label>> =
        transactionLabelDao.observeLabelsForTransaction(transactionId).map { entities -> entities.map { it.toDomain() } }

    override fun observeAllTransactionLabels(): Flow<Map<String, List<Label>>> =
        combine(labelDao.observeAll(), transactionLabelDao.observeAllCrossRefs()) { labels, crossRefs ->
            val labelsById = labels.associateBy { it.id }
            crossRefs
                .mapNotNull { crossRef -> labelsById[crossRef.labelId]?.let { crossRef.transactionId to it.toDomain() } }
                .groupBy({ it.first }, { it.second })
        }

    override suspend fun getLabel(id: String): Label? = labelDao.getById(id)?.toDomain()

    override suspend fun create(label: Label) {
        appDatabase.withTransaction { labelDao.insert(label.toEntity()) }
    }

    override suspend fun update(label: Label) {
        appDatabase.withTransaction { labelDao.update(label.toEntity()) }
    }

    override suspend fun delete(labelId: String) {
        val entity = labelDao.getById(labelId) ?: return
        appDatabase.withTransaction { labelDao.delete(entity) }
    }

    override suspend fun assign(transactionId: String, labelId: String) {
        appDatabase.withTransaction { transactionLabelDao.assign(TransactionLabelEntity(transactionId, labelId)) }
    }

    override suspend fun unassign(transactionId: String, labelId: String) {
        appDatabase.withTransaction { transactionLabelDao.unassign(transactionId, labelId) }
    }

    override suspend fun setLabelsForTransaction(transactionId: String, labelIds: List<String>) {
        appDatabase.withTransaction {
            transactionLabelDao.clearForTransaction(transactionId)
            labelIds.forEach { labelId -> transactionLabelDao.assign(TransactionLabelEntity(transactionId, labelId)) }
        }
    }
}
