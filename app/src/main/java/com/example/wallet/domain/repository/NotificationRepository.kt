package com.example.wallet.domain.repository

import com.example.wallet.domain.model.Notification
import kotlinx.coroutines.flow.Flow

/**
 * plan.md §86. [create] is called from exactly one place, `CreateNotificationUseCase` — every
 * other producer (budgets, recurring, automation, sync conflicts, ...) goes through that use
 * case, never this repository or the DAO directly, so every notification consistently gets its
 * system-notification side effect considered.
 */
interface NotificationRepository {
    fun observeAll(): Flow<List<Notification>>
    fun observeUnreadCount(): Flow<Int>
    suspend fun hasAny(): Boolean
    suspend fun create(notification: Notification)
    suspend fun markRead(id: String)
}
