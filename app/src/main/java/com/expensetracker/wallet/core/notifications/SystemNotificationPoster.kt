package com.expensetracker.wallet.core.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.expensetracker.wallet.domain.model.Notification
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

private const val CHANNEL_ID = "wallet_notifications"

/** Seam so `CreateNotificationUseCase` is unit-testable without an Android runtime. */
interface NotificationPoster {
    fun post(notification: Notification)
}

/**
 * Posts the Android system notification side effect for a Notification Center row (plan.md
 * §86) when the app is backgrounded. Channel setup lives here once so no later phase (budgets,
 * recurring, automation, ...) has to reimplement it — they all go through
 * `CreateNotificationUseCase` instead of this class directly.
 */
@Singleton
class SystemNotificationPoster @Inject constructor(
    @ApplicationContext private val context: Context,
) : NotificationPoster {
    init {
        ensureChannel()
    }

    override fun post(notification: Notification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            // Not granted (and not yet requested contextually per §59) — skip silently rather
            // than crash. The Notification Center row still exists regardless.
            return
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(notification.title)
            .setContentText(notification.body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        NotificationManagerCompat.from(context).notify(notification.id.hashCode(), builder.build())
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel = NotificationChannel(
            CHANNEL_ID,
            "Wallet notifications",
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = "Budget alerts, captured transactions, and other Wallet updates"
        }

        val manager = context.getSystemService(NotificationManager::class.java)
        manager?.createNotificationChannel(channel)
    }
}
