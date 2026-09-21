package com.example.wallet.domain.repository

import com.example.wallet.domain.model.Goal
import com.example.wallet.domain.model.GoalContribution
import kotlinx.coroutines.flow.Flow

/** Implemented in Phase 11. */
interface GoalRepository {
    fun observeGoals(): Flow<List<Goal>>
    fun observeGoal(id: String): Flow<Goal?>
    fun observeContributions(goalId: String): Flow<List<GoalContribution>>
    suspend fun create(goal: Goal)
    suspend fun contribute(goalId: String, contribution: GoalContribution)
}
