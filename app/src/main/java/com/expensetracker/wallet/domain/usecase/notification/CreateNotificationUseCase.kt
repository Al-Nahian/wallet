package com.expensetracker.wallet.domain.usecase.notification

import com.expensetracker.wallet.core.common.newId
import com.expensetracker.wallet.core.lifecycle.AppForegroundTracker
import com.expensetracker.wallet.core.notifications.NotificationPoster
import com.expensetracker.wallet.domain.model.Notification
import com.expensetracker.wallet.domain.model.NotificationType
import com.expensetracker.wallet.domain.repository.NotificationRepository
import javax.inject.Inject

/**
 * plan.md §86: the single choke point every notification producer (budgets, recurring, goals,
 * automation, sync conflicts, this phase's own welcome seed) writes through — "one write, two
 * possible surfaces." The system-notification side effect only fires when the app is
 * backgrounded; the Notification Center row is always created either way.
 */
class CreateNotificationUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val notificationPoster: NotificationPoster,
    private val appForegroundTracker: AppForegroundTracker,
) {
    suspend operator fun invoke(
        type: NotificationType,
        title: String,
        body: String,
        deepLink: String? = null,
        relatedEntityType: String? = null,
        relatedEntityId: String? = null,
    ): Notification {
        val notification = Notification(
            id = newId(),
            type = type,
            title = title,
            body = body,
            deepLink = deepLink,
            createdAt = System.currentTimeMillis(),
            readAt = null,
            relatedEntityType = relatedEntityType,
            relatedEntityId = relatedEntityId,
        )

        notificationRepository.create(notification)

        if (!appForegroundTracker.isForeground) {
            notificationPoster.post(notification)
        }

        return notification
    }
}
