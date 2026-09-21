package com.example.wallet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import com.example.wallet.core.design.WalletAppTheme
import com.example.wallet.navigation.WalletNavHost
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Transparent system bars — the app draws its own top bar background behind
        // the status bar instead of the OS reserving separate colored space for it.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT,
            ),
        )
        setContent {
            val darkTheme = isSystemInDarkTheme()
            SideEffect {
                WindowCompat.getInsetsController(window, window.decorView)
                    .isAppearanceLightStatusBars = !darkTheme
            }
            WalletAppTheme(darkTheme = darkTheme) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    WalletNavHost()
                }
            }
        }
    }
}
