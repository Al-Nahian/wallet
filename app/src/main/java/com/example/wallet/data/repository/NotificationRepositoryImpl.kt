package com.example.wallet.data.repository

import com.example.wallet.data.local.dao.NotificationDao
import com.example.wallet.domain.model.Notification
import com.example.wallet.domain.repository.NotificationRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class NotificationRepositoryImpl @Inject constructor(
    private val notificationDao: NotificationDao,
) : NotificationRepository {

    override fun observeAll(): Flow<List<Notification>> =
        notificationDao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override fun observeUnreadCount(): Flow<Int> = notificationDao.observeUnreadCount()

    override suspend fun hasAny(): Boolean = notificationDao.hasAny()

    override suspend fun create(notification: Notification) =
        notificationDao.insert(notification.toEntity())

    override suspend fun markRead(id: String) =
        notificationDao.markRead(id, System.currentTimeMillis())

    override suspend fun markAllRead() =
        notificationDao.markAllRead(System.currentTimeMillis())
}
