package com.example.wallet.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import com.example.wallet.feature.budgets.BudgetDetailScreen
import com.example.wallet.feature.budgets.BudgetFormScreen
import com.example.wallet.feature.budgets.BudgetRoutes
import com.example.wallet.feature.budgets.BudgetsScreen
import com.example.wallet.feature.categories.CategoriesScreen
import com.example.wallet.feature.categories.CategoryRoutes
import com.example.wallet.feature.dashboard.DashboardScreen
import com.example.wallet.feature.labels.LabelRoutes
import com.example.wallet.feature.labels.LabelsScreen
import com.example.wallet.feature.notifications.NotificationBadgeViewModel
import com.example.wallet.feature.notifications.NotificationCenterScreen
import com.example.wallet.feature.notifications.NotificationRoutes
import com.example.wallet.feature.profile.ProfileRoutes
import com.example.wallet.feature.profile.ProfileScreen
import com.example.wallet.feature.reports.ReportsScreen
import com.example.wallet.feature.transactions.TransactionFormScreen
import com.example.wallet.feature.transactions.TransactionRoutes
import com.example.wallet.feature.transactions.TransactionsScreen
import com.example.wallet.feature.transactions.TransferFormScreen

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
            TopLevelScaffold(
                navController = navController,
                currentRoute = WalletDestination.Home.route,
                fabOnClick = { navController.navigate(TransactionRoutes.CREATE) },
            ) {
                DashboardScreen(
                    onTransactionClick = { id -> navController.navigate(TransactionRoutes.edit(id)) },
                    onSeeAllTransactions = {
                        navController.navigate(WalletDestination.Transactions.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onManageBudgets = { navController.navigate(BudgetRoutes.LIST) },
                )
            }
        }
        composable(WalletDestination.Transactions.route) {
            TopLevelScaffold(
                navController = navController,
                currentRoute = WalletDestination.Transactions.route,
                fabOnClick = { navController.navigate(TransactionRoutes.CREATE) },
            ) {
                TransactionsScreen(
                    onAddTransaction = { navController.navigate(TransactionRoutes.CREATE) },
                    onTransactionClick = { id -> navController.navigate(TransactionRoutes.edit(id)) },
                )
            }
        }
        composable(WalletDestination.Reports.route) {
            TopLevelScaffold(
                navController = navController,
                currentRoute = WalletDestination.Reports.route,
                fabOnClick = { navController.navigate(TransactionRoutes.CREATE) },
            ) {
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
                onTransactionClick = { id -> navController.navigate(TransactionRoutes.edit(id)) },
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

        // Budget sub-screens: reached from Dashboard's Monthly Budget section (or a deep link
        // from a budget-threshold notification), same pattern as the account ones above.
        composable(BudgetRoutes.LIST) {
            BudgetsScreen(
                onBack = { navController.popBackStack() },
                onAddBudget = { navController.navigate(BudgetRoutes.CREATE) },
                onBudgetClick = { id -> navController.navigate(BudgetRoutes.detail(id)) },
            )
        }
        composable(BudgetRoutes.CREATE) {
            BudgetFormScreen(
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
            )
        }
        composable(
            route = BudgetRoutes.DETAIL_PATTERN,
            arguments = listOf(navArgument(BudgetRoutes.BUDGET_ID_ARG) { type = NavType.StringType }),
        ) {
            BudgetDetailScreen(
                onBack = { navController.popBackStack() },
                onEdit = { id -> navController.navigate(BudgetRoutes.edit(id)) },
            )
        }
        composable(
            route = BudgetRoutes.EDIT_PATTERN,
            arguments = listOf(navArgument(BudgetRoutes.BUDGET_ID_ARG) { type = NavType.StringType }),
        ) {
            BudgetFormScreen(
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
            )
        }

        // Transaction sub-screens: same pattern as the account ones above.
        composable(TransactionRoutes.CREATE) {
            TransactionFormScreen(
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
                onTransfer = {
                    navController.navigate(TransactionRoutes.TRANSFER_CREATE) {
                        popUpTo(TransactionRoutes.CREATE) { inclusive = true }
                    }
                },
            )
        }
        composable(
            route = TransactionRoutes.EDIT_PATTERN,
            arguments = listOf(navArgument(TransactionRoutes.TRANSACTION_ID_ARG) { type = NavType.StringType }),
        ) {
            TransactionFormScreen(
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
                onTransfer = { navController.navigate(TransactionRoutes.TRANSFER_CREATE) },
            )
        }
        composable(TransactionRoutes.TRANSFER_CREATE) {
            TransferFormScreen(
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
            )
        }

        // Reachable from every top-level screen's shared top bar (plan.md §83), each with its
        // own back-button Scaffold rather than the bottom-nav/FAB chrome.
        composable(ProfileRoutes.PROFILE) {
            ProfileScreen(
                onBack = { navController.popBackStack() },
                onManageCategories = { navController.navigate(CategoryRoutes.LIST) },
                onManageLabels = { navController.navigate(LabelRoutes.LIST) },
            )
        }
        composable(CategoryRoutes.LIST) {
            CategoriesScreen(onBack = { navController.popBackStack() })
        }
        composable(LabelRoutes.LIST) {
            LabelsScreen(onBack = { navController.popBackStack() })
        }
        composable(NotificationRoutes.NOTIFICATION_CENTER) {
            NotificationCenterScreen(
                onBack = { navController.popBackStack() },
                onDeepLink = { route -> navController.navigate(route) },
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
    val badgeViewModel: NotificationBadgeViewModel = hiltViewModel()
    val unreadCount by badgeViewModel.unreadCount.collectAsStateWithLifecycle()

    WalletScaffold(
        topBar = {
            WalletTopBar(
                title = screenTitles[currentRoute] ?: "Wallet",
                unreadNotificationCount = unreadCount,
                onProfileClick = { navController.navigate(ProfileRoutes.PROFILE) },
                onNotificationsClick = { navController.navigate(NotificationRoutes.NOTIFICATION_CENTER) },
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
