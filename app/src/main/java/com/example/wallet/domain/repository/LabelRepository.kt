package com.example.wallet.domain.repository

import com.example.wallet.domain.model.Label
import kotlinx.coroutines.flow.Flow

/** Implemented in Phase 7. */
interface LabelRepository {
    fun observeLabels(): Flow<List<Label>>
    fun observeLabelsForTransaction(transactionId: String): Flow<List<Label>>
    suspend fun create(label: Label)
    suspend fun assign(transactionId: String, labelId: String)
    suspend fun unassign(transactionId: String, labelId: String)
}
