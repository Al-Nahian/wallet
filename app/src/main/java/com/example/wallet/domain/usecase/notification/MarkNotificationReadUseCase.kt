package com.example.wallet.domain.usecase.notification

import com.example.wallet.domain.repository.NotificationRepository
import javax.inject.Inject

/** Idempotent: marking an already-read notification read again is a no-op (§86). */
class MarkNotificationReadUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository,
) {
    suspend operator fun invoke(notificationId: String) {
        notificationRepository.markRead(notificationId)
    }
}
