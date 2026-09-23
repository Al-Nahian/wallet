package com.example.wallet.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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
import com.example.wallet.core.design.glass.glassBackdropSource
import com.example.wallet.core.design.glass.rememberGlassBackdrop
import com.example.wallet.feature.accounts.AccountDetailScreen
import com.example.wallet.feature.accounts.AccountFormScreen
import com.example.wallet.feature.accounts.AccountRoutes
import com.example.wallet.feature.accounts.AccountsScreen
import com.example.wallet.feature.automation.AutomationRoutes
import com.example.wallet.feature.automation.AutomationSettingsScreen
import com.example.wallet.feature.automation.ReviewQueueScreen
import com.example.wallet.feature.budgets.BudgetDetailScreen
import com.example.wallet.feature.budgets.BudgetFormScreen
import com.example.wallet.feature.budgets.BudgetRoutes
import com.example.wallet.feature.budgets.BudgetsScreen
import com.example.wallet.feature.categories.CategoriesScreen
import com.example.wallet.feature.categories.CategoryRoutes
import com.example.wallet.feature.dashboard.DashboardScreen
import com.example.wallet.feature.importexport.ImportExportRoutes
import com.example.wallet.feature.importexport.ImportExportScreen
import com.example.wallet.feature.importexport.ImportWizardScreen
import com.example.wallet.feature.labels.LabelRoutes
import com.example.wallet.feature.labels.LabelsScreen
import com.example.wallet.feature.notifications.NotificationBadgeViewModel
import com.example.wallet.feature.notifications.NotificationCenterScreen
import com.example.wallet.feature.notifications.NotificationRoutes
import com.example.wallet.feature.profile.ProfileRoutes
import com.example.wallet.feature.profile.ProfileScreen
import com.example.wallet.feature.recurring.RecurringRoutes
import com.example.wallet.feature.recurring.RecurringTransactionFormScreen
import com.example.wallet.feature.recurring.RecurringTransactionsScreen
import com.example.wallet.feature.reports.ReportsScreen
import com.example.wallet.feature.transactions.TransactionFormScreen
import com.example.wallet.feature.transactions.TransactionRoutes
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
                    onManageRecurring = { navController.navigate(RecurringRoutes.LIST) },
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

        // Recurring sub-screens: reached from Dashboard's "Upcoming Recurring Payments" section
        // (or a RECURRING_DUE notification's deep link), same pattern as the budget ones above.
        composable(RecurringRoutes.LIST) {
            RecurringTransactionsScreen(
                onBack = { navController.popBackStack() },
                onAdd = { navController.navigate(RecurringRoutes.CREATE) },
                onEdit = { id -> navController.navigate(RecurringRoutes.edit(id)) },
            )
        }
        composable(RecurringRoutes.CREATE) {
            RecurringTransactionFormScreen(
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
            )
        }
        composable(
            route = RecurringRoutes.EDIT_PATTERN,
            arguments = listOf(navArgument(RecurringRoutes.RECURRING_ID_ARG) { type = NavType.StringType }),
        ) {
            RecurringTransactionFormScreen(
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
            )
        }

        // Transaction sub-screens: same pattern as the account ones above. Expense/Income/
        // Transfer are one merged form (TransactionFormScreen) — no separate transfer route.
        composable(TransactionRoutes.CREATE) {
            TransactionFormScreen(
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
            )
        }
        composable(
            route = TransactionRoutes.EDIT_PATTERN,
            arguments = listOf(navArgument(TransactionRoutes.TRANSACTION_ID_ARG) { type = NavType.StringType }),
        ) {
            TransactionFormScreen(
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
                onImportExport = { navController.navigate(ImportExportRoutes.ENTRY) },
                onAutomationSettings = { navController.navigate(AutomationRoutes.SETTINGS) },
            )
        }
        composable(AutomationRoutes.SETTINGS) {
            AutomationSettingsScreen(
                onBack = { navController.popBackStack() },
                onOpenReviewQueue = { navController.navigate(AutomationRoutes.REVIEW_QUEUE) },
            )
        }
        composable(AutomationRoutes.REVIEW_QUEUE) {
            ReviewQueueScreen(onBack = { navController.popBackStack() })
        }
        composable(ImportExportRoutes.ENTRY) {
            ImportExportScreen(
                onBack = { navController.popBackStack() },
                onFilePickedForImport = { uri ->
                    navController.navigate(ImportExportRoutes.wizard(uri.toString()))
                },
            )
        }
        composable(
            route = ImportExportRoutes.WIZARD_PATTERN,
            arguments = listOf(navArgument(ImportExportRoutes.URI_ARG) { type = NavType.StringType }),
        ) {
            ImportWizardScreen(
                onBack = { navController.popBackStack() },
                onImported = { navController.popBackStack(ImportExportRoutes.ENTRY, inclusive = false) },
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
    val backdrop = rememberGlassBackdrop()

    WalletScaffold(
        topBar = {
            WalletTopBar(
                title = screenTitles[currentRoute] ?: "Wallet",
                unreadNotificationCount = unreadCount,
                onProfileClick = { navController.navigate(ProfileRoutes.PROFILE) },
                onNotificationsClick = { navController.navigate(NotificationRoutes.NOTIFICATION_CENTER) },
            )
        },
    ) { paddingValues ->
        // The nav bar overlays the content rather than sitting in the Scaffold's bottomBar slot:
        // a reserved slot would leave an opaque page-background strip behind the floating pill,
        // so nothing would show through its glass. Screens add WalletBottomNavSpace to their own
        // bottom content padding so their last item still scrolls clear of it.
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            // Recorded so the nav bar can draw it back blurred behind itself. The opaque
            // background is part of the recording on purpose: the blurred copy has to fully
            // cover the sharp original underneath it, or both would show at once.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .glassBackdropSource(backdrop, MaterialTheme.colorScheme.surface),
            ) {
                content()
            }
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
                fabOnClick = fabOnClick,
                fabContentDescription = if (currentRoute == WalletDestination.Accounts.route) {
                    "Add account"
                } else {
                    "Add transaction"
                },
                modifier = Modifier.align(Alignment.BottomCenter),
                backdrop = backdrop,
            )
        }
    }
}
