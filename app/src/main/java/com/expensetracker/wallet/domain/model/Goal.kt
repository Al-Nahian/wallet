package com.expensetracker.wallet.domain.model

data class Goal(
    val id: String,
    val name: String,
    val targetAmountMinor: Long,
    val currentAmountMinor: Long,
    val currency: String,
    val targetDate: Long?,
    val accountId: String?,
    val createdAt: Long,
    val updatedAt: Long,
)

data class GoalContribution(
    val id: String,
    val goalId: String,
    val amountMinor: Long,
    val date: Long,
    val note: String?,
)
