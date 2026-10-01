package com.expensetracker.wallet.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.expensetracker.wallet.data.local.entity.AutomationCandidateEntity
import com.expensetracker.wallet.domain.model.AutomationCandidateStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface AutomationCandidateDao {
    @Query("SELECT * FROM automation_candidates WHERE status = 'PENDING' ORDER BY date DESC")
    fun observePending(): Flow<List<AutomationCandidateEntity>>

    @Query("SELECT COUNT(*) FROM automation_candidates WHERE status = 'PENDING'")
    fun observePendingCount(): Flow<Int>

    @Query("SELECT * FROM automation_candidates WHERE id = :id")
    suspend fun getById(id: String): AutomationCandidateEntity?

    @Query("SELECT * FROM automation_candidates WHERE sourceReference = :sourceReference LIMIT 1")
    suspend fun findBySourceReference(sourceReference: String): AutomationCandidateEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(candidate: AutomationCandidateEntity)

    @Query("UPDATE automation_candidates SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: String, status: AutomationCandidateStatus)
}
