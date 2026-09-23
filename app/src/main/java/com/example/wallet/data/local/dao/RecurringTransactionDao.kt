package com.example.wallet.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.wallet.data.local.entity.RecurringTransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecurringTransactionDao {
    @Query("SELECT * FROM recurring_transactions WHERE isActive = 1 ORDER BY nextDate")
    fun observeActive(): Flow<List<RecurringTransactionEntity>>

    @Query("SELECT * FROM recurring_transactions WHERE id = :id")
    fun observeById(id: String): Flow<RecurringTransactionEntity?>

    @Query("SELECT * FROM recurring_transactions WHERE id = :id")
    suspend fun getById(id: String): RecurringTransactionEntity?

    @Query("SELECT * FROM recurring_transactions WHERE nextDate <= :beforeOrAt AND isActive = 1")
    suspend fun getDue(beforeOrAt: Long): List<RecurringTransactionEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(recurring: RecurringTransactionEntity)

    @Update
    suspend fun update(recurring: RecurringTransactionEntity)

    @Query("DELETE FROM recurring_transactions WHERE id = :id")
    suspend fun delete(id: String)
}
