package com.example.wallet.data.local.seed

import com.example.wallet.domain.model.NotificationType
import com.example.wallet.domain.repository.NotificationRepository
import com.example.wallet.domain.usecase.notification.CreateNotificationUseCase
import javax.inject.Inject

/**
 * Seeds a single welcome notification on first launch (plans/04-notification-center-account-
 * shell.md) so there's something real to read/mark-read during testing. Goes through
 * `CreateNotificationUseCase`, not the DAO, so it stays the single writer to `notifications`.
 */
class NotificationSeeder @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val createNotificationUseCase: CreateNotificationUseCase,
) {
    suspend fun seedIfEmpty() {
        if (notificationRepository.hasAny()) return

        createNotificationUseCase(
            type = NotificationType.SYSTEM,
            title = "Welcome to Wallet",
            body = "This is where you'll see updates about your accounts, budgets, and " +
                "transactions captured automatically.",
        )
    }
}
