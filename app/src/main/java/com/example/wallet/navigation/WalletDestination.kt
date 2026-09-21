package com.example.wallet.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Receipt
import com.example.wallet.core.design.components.WalletBottomNavItem

/** The 4 primary destinations from plan.md §8. */
sealed class WalletDestination(val route: String) {
    data object Home : WalletDestination("home")
    data object Transactions : WalletDestination("transactions")
    data object Reports : WalletDestination("reports")
    data object Accounts : WalletDestination("accounts")
}

val walletBottomNavItems = listOf(
    WalletBottomNavItem(WalletDestination.Home.route, "Home", Icons.Filled.Home),
    WalletBottomNavItem(WalletDestination.Transactions.route, "Transactions", Icons.Filled.Receipt),
    WalletBottomNavItem(WalletDestination.Reports.route, "Reports", Icons.Filled.BarChart),
    WalletBottomNavItem(WalletDestination.Accounts.route, "Accounts", Icons.Filled.AccountBalanceWallet),
)
