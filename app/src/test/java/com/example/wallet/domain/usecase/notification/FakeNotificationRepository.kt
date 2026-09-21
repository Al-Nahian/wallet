package com.example.wallet.domain.usecase.notification

import com.example.wallet.domain.model.Notification
import com.example.wallet.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/** In-memory fake mirroring the real DAO's idempotent mark-read semantics (COALESCE). */
class FakeNotificationRepository : NotificationRepository {
    private val notifications = MutableStateFlow<List<Notification>>(emptyList())

    override fun observeAll(): Flow<List<Notification>> = notifications

    override fun observeUnreadCount(): Flow<Int> =
        notifications.map { list -> list.count { it.isUnread } }

    override suspend fun hasAny(): Boolean = notifications.value.isNotEmpty()

    override suspend fun create(notification: Notification) {
        notifications.value = notifications.value + notification
    }

    override suspend fun markRead(id: String) {
        notifications.value = notifications.value.map { notification ->
            if (notification.id == id && notification.readAt == null) {
                notification.copy(readAt = System.currentTimeMillis())
            } else {
                notification
            }
        }
    }
}
