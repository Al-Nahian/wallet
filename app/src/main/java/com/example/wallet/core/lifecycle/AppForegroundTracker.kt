package com.example.wallet.core.lifecycle

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Tracks whether the app currently has any activity in the foreground, so
 * `CreateNotificationUseCase` (plan.md §86) knows when the system-notification side effect is
 * actually needed. Registered on `ProcessLifecycleOwner` once, in `WalletApplication.onCreate()`.
 */
@Singleton
class AppForegroundTracker @Inject constructor() : DefaultLifecycleObserver {
    var isForeground: Boolean = false
        private set

    override fun onStart(owner: LifecycleOwner) {
        isForeground = true
    }

    override fun onStop(owner: LifecycleOwner) {
        isForeground = false
    }
}
