package com.expensetracker.wallet.domain.usecase.notification

import com.expensetracker.wallet.core.notifications.NotificationPoster
import com.expensetracker.wallet.domain.model.Notification

class FakeNotificationPoster : NotificationPoster {
    val posted = mutableListOf<Notification>()

    override fun post(notification: Notification) {
        posted += notification
    }
}
