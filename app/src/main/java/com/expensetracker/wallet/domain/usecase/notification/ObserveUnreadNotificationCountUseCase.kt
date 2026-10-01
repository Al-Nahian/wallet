package com.expensetracker.wallet.domain.usecase.notification

import com.expensetracker.wallet.domain.repository.NotificationRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveUnreadNotificationCountUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository,
) {
    operator fun invoke(): Flow<Int> = notificationRepository.observeUnreadCount()
}
