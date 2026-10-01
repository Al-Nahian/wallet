package com.expensetracker.wallet.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** plan.md §19. `SUM(goal_contributions.amountMinor) == goal.currentAmountMinor` is
 * enforced by `ContributeToGoalUseCase` (Phase 10), not by the database. */
@Entity(
    tableName = "goal_contributions",
    foreignKeys = [
        ForeignKey(
            entity = GoalEntity::class,
            parentColumns = ["id"],
            childColumns = ["goalId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("goalId")],
)
data class GoalContributionEntity(
    @PrimaryKey val id: String,
    val goalId: String,
    val amountMinor: Long,
    val date: Long,
    val note: String? = null,
)
