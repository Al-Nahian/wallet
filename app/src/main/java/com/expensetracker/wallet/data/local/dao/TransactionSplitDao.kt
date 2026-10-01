package com.expensetracker.wallet.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.expensetracker.wallet.data.local.entity.TransactionSplitEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionSplitDao {
    @Query("SELECT * FROM transaction_splits WHERE transactionId = :transactionId")
    fun observeByTransaction(transactionId: String): Flow<List<TransactionSplitEntity>>

    @Query("SELECT * FROM transaction_splits")
    fun observeAll(): Flow<List<TransactionSplitEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM transaction_splits WHERE categoryId = :categoryId)")
    suspend fun existsByCategory(categoryId: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(splits: List<TransactionSplitEntity>)

    @Query("DELETE FROM transaction_splits WHERE transactionId = :transactionId")
    suspend fun deleteByTransaction(transactionId: String)

    @Delete
    suspend fun delete(split: TransactionSplitEntity)
}
