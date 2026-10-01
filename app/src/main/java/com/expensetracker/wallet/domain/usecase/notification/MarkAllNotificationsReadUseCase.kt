package com.expensetracker.wallet.domain.usecase.notification

import com.expensetracker.wallet.domain.repository.NotificationRepository
import javax.inject.Inject

/** Idempotent, same as [MarkNotificationReadUseCase] — called once when Notification Center opens
 * so the bell's unread badge clears. */
class MarkAllNotificationsReadUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository,
) {
    suspend operator fun invoke() {
        notificationRepository.markAllRead()
    }
}
