package com.expensetracker.wallet.domain.repository

import com.expensetracker.wallet.domain.model.Label
import kotlinx.coroutines.flow.Flow

interface LabelRepository {
    fun observeLabels(): Flow<List<Label>>
    fun observeLabelsForTransaction(transactionId: String): Flow<List<Label>>

    /** Every transaction's labels at once, keyed by transaction id — for list/detail screens
     * that render many transactions without an N+1 Flow per row. */
    fun observeAllTransactionLabels(): Flow<Map<String, List<Label>>>

    suspend fun getLabel(id: String): Label?
    suspend fun create(label: Label)
    suspend fun update(label: Label)
    suspend fun delete(labelId: String)
    suspend fun assign(transactionId: String, labelId: String)
    suspend fun unassign(transactionId: String, labelId: String)

    /** Replaces a transaction's full label set atomically (plan.md §72) — the shape the
     * transaction form's multi-select picker actually needs. */
    suspend fun setLabelsForTransaction(transactionId: String, labelIds: List<String>)
}
