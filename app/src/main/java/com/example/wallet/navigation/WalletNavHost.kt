package com.example.wallet.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.wallet.core.design.components.WalletBottomNavigation
import com.example.wallet.core.design.components.WalletScaffold
import com.example.wallet.core.design.components.WalletTopBar
import com.example.wallet.feature.accounts.AccountDetailScreen
import com.example.wallet.feature.accounts.AccountFormScreen
import com.example.wallet.feature.accounts.AccountRoutes
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

    NavHost(navController = navController, startDestination = WalletDestination.Home.route) {
        composable(WalletDestination.Home.route) {
            TopLevelScaffold(navController, WalletDestination.Home.route, fabOnClick = { }) {
                DashboardScreen()
            }
        }
        composable(WalletDestination.Transactions.route) {
            TopLevelScaffold(navController, WalletDestination.Transactions.route, fabOnClick = { }) {
                // No-op FAB in this tab until Phase 5 (Transactions Core) wires add-expense/income.
                TransactionsScreen()
            }
        }
        composable(WalletDestination.Reports.route) {
            TopLevelScaffold(navController, WalletDestination.Reports.route, fabOnClick = { }) {
                ReportsScreen()
            }
        }
        composable(WalletDestination.Accounts.route) {
            TopLevelScaffold(
                navController = navController,
                currentRoute = WalletDestination.Accounts.route,
                fabOnClick = { navController.navigate(AccountRoutes.CREATE) },
            ) {
                AccountsScreen(
                    onAccountClick = { id -> navController.navigate(AccountRoutes.detail(id)) },
                    onAddAccount = { navController.navigate(AccountRoutes.CREATE) },
                )
            }
        }

        // Account sub-screens: each renders its own back-button Scaffold, outside the
        // shared bottom-nav/FAB chrome above (plans/03-accounts.md's create/edit/detail flow).
        composable(AccountRoutes.CREATE) {
            AccountFormScreen(
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
            )
        }
        composable(
            route = AccountRoutes.DETAIL_PATTERN,
            arguments = listOf(navArgument(AccountRoutes.ACCOUNT_ID_ARG) { type = NavType.StringType }),
        ) {
            AccountDetailScreen(
                onBack = { navController.popBackStack() },
                onEdit = { id -> navController.navigate(AccountRoutes.edit(id)) },
                onArchived = { navController.popBackStack() },
            )
        }
        composable(
            route = AccountRoutes.EDIT_PATTERN,
            arguments = listOf(navArgument(AccountRoutes.ACCOUNT_ID_ARG) { type = NavType.StringType }),
        ) {
            AccountFormScreen(
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
            )
        }
    }
}

/** Shared chrome (profile/bell top bar, bottom nav, FAB) for the 4 primary destinations. */
@Composable
private fun TopLevelScaffold(
    navController: NavHostController,
    currentRoute: String,
    fabOnClick: () -> Unit,
    content: @Composable () -> Unit,
) {
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
            FloatingActionButton(onClick = fabOnClick) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = if (currentRoute == WalletDestination.Accounts.route) {
                        "Add account"
                    } else {
                        "Add transaction"
                    },
                )
            }
        },
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            content()
        }
    }
}
