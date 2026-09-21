package com.example.wallet.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.wallet.data.local.entity.LabelEntity
import com.example.wallet.data.local.entity.TransactionLabelEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LabelDao {
    @Query("SELECT * FROM labels ORDER BY name")
    fun observeAll(): Flow<List<LabelEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(label: LabelEntity)

    @Update
    suspend fun update(label: LabelEntity)

    @Delete
    suspend fun delete(label: LabelEntity)
}

@Dao
interface TransactionLabelDao {
    @Query(
        "SELECT labels.* FROM labels " +
            "INNER JOIN transaction_labels ON labels.id = transaction_labels.labelId " +
            "WHERE transaction_labels.transactionId = :transactionId",
    )
    fun observeLabelsForTransaction(transactionId: String): Flow<List<LabelEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun assign(crossRef: TransactionLabelEntity)

    @Query("DELETE FROM transaction_labels WHERE transactionId = :transactionId AND labelId = :labelId")
    suspend fun unassign(transactionId: String, labelId: String)

    @Query("DELETE FROM transaction_labels WHERE transactionId = :transactionId")
    suspend fun clearForTransaction(transactionId: String)
}
