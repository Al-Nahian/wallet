package com.example.wallet

import android.app.Application
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.wallet.core.lifecycle.AppForegroundTracker
import com.example.wallet.data.local.seed.CategorySeeder
import com.example.wallet.data.local.seed.NotificationSeeder
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@HiltAndroidApp
class WalletApplication : Application() {

    @Inject
    lateinit var categorySeeder: CategorySeeder

    @Inject
    lateinit var notificationSeeder: NotificationSeeder

    @Inject
    lateinit var appForegroundTracker: AppForegroundTracker

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        ProcessLifecycleOwner.get().lifecycle.addObserver(appForegroundTracker)
        applicationScope.launch {
            categorySeeder.seedIfEmpty()
            notificationSeeder.seedIfEmpty()
        }
    }
}
