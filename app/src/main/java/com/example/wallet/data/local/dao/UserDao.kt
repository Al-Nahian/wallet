package com.example.wallet.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.wallet.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    // Guest mode = no row (plan.md §85), so this app is single-user locally: LIMIT 1 is enough.
    @Query("SELECT * FROM users LIMIT 1")
    fun observeCurrentUser(): Flow<UserEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(user: UserEntity)
}
