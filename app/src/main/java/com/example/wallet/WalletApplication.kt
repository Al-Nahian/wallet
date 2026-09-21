package com.example.wallet

import android.app.Application
import com.example.wallet.data.local.seed.CategorySeeder
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

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        applicationScope.launch { categorySeeder.seedIfEmpty() }
    }
}
