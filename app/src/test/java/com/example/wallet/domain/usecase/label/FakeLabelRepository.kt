package com.example.wallet.domain.usecase.label

import com.example.wallet.domain.model.Label
import com.example.wallet.domain.repository.LabelRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/** In-memory fake so use-case tests don't need Room/Android. */
class FakeLabelRepository : LabelRepository {
    private val labels = MutableStateFlow<Map<String, Label>>(emptyMap())
    private val assignments = MutableStateFlow<Map<String, Set<String>>>(emptyMap())

    override fun observeLabels(): Flow<List<Label>> = labels.map { it.values.toList() }

    override fun observeLabelsForTransaction(transactionId: String): Flow<List<Label>> =
        assignments.map { assignmentsMap ->
            assignmentsMap[transactionId].orEmpty().mapNotNull { labels.value[it] }
        }

    override fun observeAllTransactionLabels(): Flow<Map<String, List<Label>>> =
        assignments.map { assignmentsMap ->
            assignmentsMap.mapValues { (_, labelIds) -> labelIds.mapNotNull { labels.value[it] } }
        }

    override suspend fun getLabel(id: String): Label? = labels.value[id]

    override suspend fun create(label: Label) {
        labels.value = labels.value + (label.id to label)
    }

    override suspend fun update(label: Label) {
        labels.value = labels.value + (label.id to label)
    }

    override suspend fun delete(labelId: String) {
        labels.value = labels.value - labelId
        assignments.value = assignments.value.mapValues { (_, ids) -> ids - labelId }
    }

    override suspend fun assign(transactionId: String, labelId: String) {
        val current = assignments.value[transactionId].orEmpty()
        assignments.value = assignments.value + (transactionId to (current + labelId))
    }

    override suspend fun unassign(transactionId: String, labelId: String) {
        val current = assignments.value[transactionId].orEmpty()
        assignments.value = assignments.value + (transactionId to (current - labelId))
    }

    override suspend fun setLabelsForTransaction(transactionId: String, labelIds: List<String>) {
        assignments.value = assignments.value + (transactionId to labelIds.toSet())
    }
}
