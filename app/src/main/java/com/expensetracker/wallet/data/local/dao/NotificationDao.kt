package com.expensetracker.wallet.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.expensetracker.wallet.data.local.entity.NotificationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<NotificationEntity>>

    @Query("SELECT COUNT(*) FROM notifications WHERE readAt IS NULL")
    fun observeUnreadCount(): Flow<Int>

    @Query("SELECT EXISTS(SELECT 1 FROM notifications LIMIT 1)")
    suspend fun hasAny(): Boolean

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(notification: NotificationEntity)

    // COALESCE keeps the first mark-read timestamp — a second call is a no-op (idempotent).
    @Query("UPDATE notifications SET readAt = COALESCE(readAt, :readAt) WHERE id = :id")
    suspend fun markRead(id: String, readAt: Long)

    @Query("UPDATE notifications SET readAt = COALESCE(readAt, :readAt) WHERE readAt IS NULL")
    suspend fun markAllRead(readAt: Long)
}
