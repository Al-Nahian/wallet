package com.example.wallet.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.wallet.data.local.entity.TransactionEntity
import com.example.wallet.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions WHERE deletedAt IS NULL ORDER BY date DESC")
    fun observeAll(): Flow<List<TransactionEntity>>

    @Query(
        "SELECT * FROM transactions WHERE deletedAt IS NULL AND accountId = :accountId " +
            "ORDER BY date DESC",
    )
    fun observeByAccount(accountId: String): Flow<List<TransactionEntity>>

    @Query(
        "SELECT * FROM transactions WHERE deletedAt IS NULL AND date BETWEEN :startInclusive " +
            "AND :endInclusive ORDER BY date DESC",
    )
    fun observeByDateRange(startInclusive: Long, endInclusive: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE deletedAt IS NULL AND categoryId = :categoryId ORDER BY date DESC")
    fun observeByCategory(categoryId: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getById(id: String): TransactionEntity?

    @Query(
        "SELECT COALESCE(SUM(amountMinor), 0) FROM transactions WHERE deletedAt IS NULL " +
            "AND accountId = :accountId AND type = :type",
    )
    suspend fun sumByAccountAndType(accountId: String, type: TransactionType): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(transaction: TransactionEntity)

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Query("UPDATE transactions SET deletedAt = :deletedAt, updatedAt = :deletedAt WHERE id = :id")
    suspend fun softDelete(id: String, deletedAt: Long)
}
