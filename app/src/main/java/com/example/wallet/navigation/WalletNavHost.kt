package com.example.wallet.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.wallet.core.design.components.WalletBottomNavigation
import com.example.wallet.core.design.components.WalletScaffold
import com.example.wallet.core.design.components.WalletTopBar
import com.example.wallet.feature.accounts.AccountsScreen
import com.example.wallet.feature.dashboard.DashboardScreen
import com.example.wallet.feature.reports.ReportsScreen
import com.example.wallet.feature.transactions.TransactionsScreen

private val screenTitles = mapOf(
    WalletDestination.Home.route to "Wallet",
    WalletDestination.Transactions.route to "Transactions",
    WalletDestination.Reports.route to "Reports",
    WalletDestination.Accounts.route to "Accounts",
)

@Composable
fun WalletNavHost() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: WalletDestination.Home.route

    WalletScaffold(
        topBar = {
            WalletTopBar(
                title = screenTitles[currentRoute] ?: "Wallet",
                // Profile → Account screen and bell → Notification Center are both
                // no-ops until Phase 4 (Notification Center & Account Shell) wires
                // their real destinations and the live unread count.
                onProfileClick = { },
                onNotificationsClick = { },
            )
        },
        bottomBar = {
            WalletBottomNavigation(
                items = walletBottomNavItems,
                selectedRoute = currentRoute,
                onItemSelected = { route ->
                    navController.navigate(route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
            )
        },
        floatingActionButton = {
            // No-op in Phase 1 — the real add-transaction flow lands in Phase 5.
            FloatingActionButton(onClick = { }) {
                Icon(Icons.Filled.Add, contentDescription = "Add transaction")
            }
        },
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = WalletDestination.Home.route,
            modifier = Modifier.padding(paddingValues),
        ) {
            composable(WalletDestination.Home.route) { DashboardScreen() }
            composable(WalletDestination.Transactions.route) { TransactionsScreen() }
            composable(WalletDestination.Reports.route) { ReportsScreen() }
            composable(WalletDestination.Accounts.route) { AccountsScreen() }
        }
    }
}
