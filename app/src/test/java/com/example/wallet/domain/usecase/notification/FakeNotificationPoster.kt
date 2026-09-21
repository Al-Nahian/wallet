package com.example.wallet.domain.usecase.notification

import com.example.wallet.core.notifications.NotificationPoster
import com.example.wallet.domain.model.Notification

class FakeNotificationPoster : NotificationPoster {
    val posted = mutableListOf<Notification>()

    override fun post(notification: Notification) {
        posted += notification
    }
}
