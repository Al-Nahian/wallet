package com.expensetracker.wallet

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.work.Configuration
import com.expensetracker.wallet.core.lifecycle.AppForegroundTracker
import com.expensetracker.wallet.core.work.scheduleRecurringTransactionWork
import com.expensetracker.wallet.data.local.seed.CategorySeeder
import com.expensetracker.wallet.data.local.seed.NotificationSeeder
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@HiltAndroidApp
class WalletApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var categorySeeder: CategorySeeder

    @Inject
    lateinit var notificationSeeder: NotificationSeeder

    @Inject
    lateinit var appForegroundTracker: AppForegroundTracker

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setWorkerFactory(workerFactory).build()

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        ProcessLifecycleOwner.get().lifecycle.addObserver(appForegroundTracker)
        applicationScope.launch {
            categorySeeder.seedIfEmpty()
            notificationSeeder.seedIfEmpty()
        }
        scheduleRecurringTransactionWork(this)
    }
}
