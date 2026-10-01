package com.expensetracker.wallet.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.expensetracker.wallet.data.local.entity.GoalContributionEntity
import com.expensetracker.wallet.data.local.entity.GoalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {
    @Query("SELECT * FROM goals ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<GoalEntity>>

    @Query("SELECT * FROM goals WHERE id = :id")
    fun observeById(id: String): Flow<GoalEntity?>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(goal: GoalEntity)

    @Update
    suspend fun update(goal: GoalEntity)
}

@Dao
interface GoalContributionDao {
    @Query("SELECT * FROM goal_contributions WHERE goalId = :goalId ORDER BY date DESC")
    fun observeByGoal(goalId: String): Flow<List<GoalContributionEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(contribution: GoalContributionEntity)
}
