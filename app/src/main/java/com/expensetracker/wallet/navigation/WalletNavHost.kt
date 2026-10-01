package com.expensetracker.wallet.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.expensetracker.wallet.core.design.components.TemplateFabAction
import com.expensetracker.wallet.core.design.components.TemplateFabMenu
import com.expensetracker.wallet.core.design.components.TemplateShortcutPickerDialog
import com.expensetracker.wallet.core.design.components.WalletBottomNavigation
import com.expensetracker.wallet.core.design.components.WalletScaffold
import com.expensetracker.wallet.core.design.components.WalletTopBar
import com.expensetracker.wallet.core.design.glass.LiquidDarkPageBackground
import com.expensetracker.wallet.feature.accounts.AccountDetailScreen
import com.expensetracker.wallet.feature.accounts.AccountFormScreen
import com.expensetracker.wallet.feature.accounts.AccountRoutes
import com.expensetracker.wallet.feature.accounts.AccountsScreen
import com.expensetracker.wallet.feature.automation.AutomationRoutes
import com.expensetracker.wallet.feature.automation.AutomationSettingsScreen
import com.expensetracker.wallet.feature.automation.ReviewQueueScreen
import com.expensetracker.wallet.feature.budgets.BudgetDetailScreen
import com.expensetracker.wallet.feature.budgets.BudgetFormScreen
import com.expensetracker.wallet.feature.budgets.BudgetRoutes
import com.expensetracker.wallet.feature.budgets.BudgetsScreen
import com.expensetracker.wallet.feature.categories.CategoriesScreen
import com.expensetracker.wallet.feature.categories.CategoryRoutes
import com.expensetracker.wallet.feature.dashboard.DashboardScreen
import com.expensetracker.wallet.feature.importexport.ImportExportRoutes
import com.expensetracker.wallet.feature.importexport.ImportExportScreen
import com.expensetracker.wallet.feature.importexport.ImportWizardScreen
import com.expensetracker.wallet.feature.labels.LabelRoutes
import com.expensetracker.wallet.feature.templates.TemplateRoutes
import com.expensetracker.wallet.feature.templates.TemplateShortcutsViewModel
import com.expensetracker.wallet.feature.templates.TemplatesScreen
import com.expensetracker.wallet.feature.labels.LabelsScreen
import com.expensetracker.wallet.feature.notifications.NotificationBadgeViewModel
import com.expensetracker.wallet.feature.notifications.NotificationCenterScreen
import com.expensetracker.wallet.feature.notifications.NotificationRoutes
import com.expensetracker.wallet.feature.profile.ProfileRoutes
import com.expensetracker.wallet.feature.profile.ProfileScreen
import com.expensetracker.wallet.feature.recurring.RecurringRoutes
import com.expensetracker.wallet.feature.recurring.RecurringTransactionFormScreen
import com.expensetracker.wallet.feature.recurring.RecurringTransactionsScreen
import com.expensetracker.wallet.feature.reports.ReportsScreen
import com.expensetracker.wallet.feature.transactions.TransactionFormScreen
import com.expensetracker.wallet.feature.transactions.TransactionRoutes
import com.expensetracker.wallet.feature.transactions.TransactionsScreen

/** The 4 bottom-nav destinations swap via `popUpTo`/`restoreState`, not a push/pop stack, so they
 * cross-fade like tab switches instead of sliding like the rest of the screens below (a slide
 * would look like a step forward/back in a hierarchy that doesn't exist between tabs). */
private val topLevelRoutes = setOf(
    WalletDestination.Home.route,
    WalletDestination.Transactions.route,
    WalletDestination.Reports.route,
    WalletDestination.Accounts.route,
)

private fun AnimatedContentTransitionScope<NavBackStackEntry>.isTopLevelSwitch(): Boolean =
    initialState.destination.route in topLevelRoutes && targetState.destination.route in topLevelRoutes

private const val TransitionDurationMs = 260
private val TransitionEasing = FastOutSlowInEasing

private val screenTitles = mapOf(
    WalletDestination.Home.route to "Wallet",
    WalletDestination.Transactions.route to "Transactions",
    WalletDestination.Reports.route to "Reports",
    WalletDestination.Accounts.route to "Accounts",
)

@Composable
fun WalletNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = WalletDestination.Home.route,
        enterTransition = {
            if (isTopLevelSwitch()) {
                fadeIn(animationSpec = tween(TransitionDurationMs))
            } else {
                slideInHorizontally(
                    initialOffsetX = { it / 3 },
                    animationSpec = tween(TransitionDurationMs, easing = TransitionEasing),
                ) + fadeIn(animationSpec = tween(TransitionDurationMs))
            }
        },
        exitTransition = {
            if (isTopLevelSwitch()) {
                fadeOut(animationSpec = tween(TransitionDurationMs))
            } else {
                slideOutHorizontally(
                    targetOffsetX = { -it / 4 },
                    animationSpec = tween(TransitionDurationMs, easing = TransitionEasing),
                ) + fadeOut(animationSpec = tween(TransitionDurationMs / 2))
            }
        },
        popEnterTransition = {
            if (isTopLevelSwitch()) {
                fadeIn(animationSpec = tween(TransitionDurationMs))
            } else {
                slideInHorizontally(
                    initialOffsetX = { -it / 4 },
                    animationSpec = tween(TransitionDurationMs, easing = TransitionEasing),
                ) + fadeIn(animationSpec = tween(TransitionDurationMs))
            }
        },
        popExitTransition = {
            if (isTopLevelSwitch()) {
                fadeOut(animationSpec = tween(TransitionDurationMs))
            } else {
                slideOutHorizontally(
                    targetOffsetX = { it / 3 },
                    animationSpec = tween(TransitionDurationMs, easing = TransitionEasing),
                ) + fadeOut(animationSpec = tween(TransitionDurationMs / 2))
            }
        },
    ) {
        composable(WalletDestination.Home.route) {
            // Home-only: tapping the FAB with saved templates fans out a tiny two-icon liquid
            // speed-dial (github.com/jurajkusnier/fluid-bottom-navigation's gooey-blob
            // technique) — "Add New Transaction" and "Select Template" — instead of navigating
            // straight to a blank form. With no templates saved yet there's nothing to pick from,
            // so the FAB keeps its old direct-navigate behavior.
            val templateShortcutsViewModel: TemplateShortcutsViewModel = hiltViewModel()
            val shortcutTemplates by templateShortcutsViewModel.templates.collectAsStateWithLifecycle()
            val shortcutCategories by templateShortcutsViewModel.categories.collectAsStateWithLifecycle()
            var isFabMenuExpanded by remember { mutableStateOf(false) }
            var showTemplatePicker by remember { mutableStateOf(false) }

            TopLevelScaffold(
                navController = navController,
                currentRoute = WalletDestination.Home.route,
                fabOnClick = {
                    if (shortcutTemplates.isEmpty()) {
                        navController.navigate(TransactionRoutes.CREATE)
                    } else {
                        isFabMenuExpanded = !isFabMenuExpanded
                    }
                },
                fabMenuExpanded = isFabMenuExpanded,
                onDismissFabMenu = { isFabMenuExpanded = false },
                fabMenu = { fabSize, liquidBackdrop ->
                    TemplateFabMenu(
                        expanded = isFabMenuExpanded,
                        fabSize = fabSize,
                        liquidFabBackdrop = liquidBackdrop,
                        onAction = { action ->
                            isFabMenuExpanded = false
                            when (action) {
                                TemplateFabAction.ADD_NEW_TRANSACTION -> navController.navigate(TransactionRoutes.CREATE)
                                TemplateFabAction.SELECT_TEMPLATE -> showTemplatePicker = true
                                TemplateFabAction.ADD_ACCOUNT -> navController.navigate(AccountRoutes.CREATE)
                            }
                        },
                    )
                },
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
                    onAccountClick = { id -> navController.navigate(AccountRoutes.detail(id)) },
                    onSeeAllAccounts = {
                        navController.navigate(WalletDestination.Accounts.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onOpenReports = {
                        navController.navigate(WalletDestination.Reports.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }

            if (showTemplatePicker) {
                TemplateShortcutPickerDialog(
                    templates = shortcutTemplates,
                    categories = shortcutCategories,
                    onSelected = { templateId ->
                        showTemplatePicker = false
                        navController.navigate(TransactionRoutes.createFromTemplate(templateId))
                    },
                    onCreateNew = {
                        showTemplatePicker = false
                        navController.navigate(TemplateRoutes.LIST)
                    },
                    onDismiss = { showTemplatePicker = false },
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
        composable(
            route = TransactionRoutes.CREATE_PATTERN,
            arguments = listOf(
                navArgument(TransactionRoutes.TEMPLATE_ID_ARG) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
            ),
        ) {
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
                onManageTemplates = { navController.navigate(TemplateRoutes.LIST) },
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
        composable(TemplateRoutes.LIST) {
            TemplatesScreen(onBack = { navController.popBackStack() })
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
    fabMenuExpanded: Boolean = false,
    onDismissFabMenu: () -> Unit = {},
    fabMenu: (@Composable (fabSize: Dp, liquidFabBackdrop: LayerBackdrop?) -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val badgeViewModel: NotificationBadgeViewModel = hiltViewModel()
    val unreadCount by badgeViewModel.unreadCount.collectAsStateWithLifecycle()
    // Shared by both the bottom bar's blur and the FAB's refraction (see GlassBottomBar's doc) —
    // capturing the whole screen content once instead of twice measurably cut scroll jank
    // (confirmed via `dumpsys gfxinfo framestats`), with no visual difference since both were
    // always reading the exact same content anyway.
    val liquidFabBackdrop = rememberLayerBackdrop()
    val isDark = isSystemInDarkTheme()

    // The reference design's dark-mode background is a real glow-blob image, not flat black —
    // drawn once here, inside the blur capture, rather than per-screen.
    Box(modifier = Modifier.fillMaxSize()) {
        // Recorded so the nav bar/FAB can draw it back blurred/refracted behind themselves.
        // The capture wraps background + scaffold as one fullscreen layer with the nav pill as
        // a sibling outside it (a capture may never contain its own reader — recording the pill
        // from inside itself crashes the render thread). One background copy only: an earlier
        // version drew a second copy inside a smaller padded box, whose different image crop
        // left a visible hairline seam below the top bar. In dark mode there is deliberately NO
        // opaque base paint — the glow image is already a soft blur itself, so blurring it again
        // barely changes it. Light mode keeps an opaque surface base, since its background is a
        // flat color a transparency seam would still show against.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .let { if (isDark) it else it.background(MaterialTheme.colorScheme.surface) }
                .layerBackdrop(liquidFabBackdrop),
        ) {
            if (isDark) {
                LiquidDarkPageBackground()
            }
            WalletScaffold(
                containerColor = Color.Transparent,
                topBar = {
                    WalletTopBar(
                        title = screenTitles[currentRoute] ?: "Wallet",
                        unreadNotificationCount = unreadCount,
                        onProfileClick = { navController.navigate(ProfileRoutes.PROFILE) },
                        onNotificationsClick = { navController.navigate(NotificationRoutes.NOTIFICATION_CENTER) },
                    )
                },
            ) { paddingValues ->
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                    content()
                }
            }
        }
        // Tapping anywhere outside the fanned-out template menu closes it — sits above the
        // page content but below the nav bar/FAB/menu itself in z-order, so those stay tappable.
        if (fabMenuExpanded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onDismissFabMenu,
                    ),
            )
        }
        // The nav bar overlays the content rather than sitting in the Scaffold's bottomBar
        // slot: a reserved slot would leave an opaque page-background strip behind the
        // floating pill, so nothing would show through its glass. Kept outside the capture
        // above (it reads that capture). navigationBarsPadding replaces the Scaffold content
        // inset it previously sat inside, so the pill still clears the system gesture bar.
        // Screens add WalletBottomNavSpace to their own bottom content padding so their last
        // item still scrolls clear of it.
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
            fabExpanded = fabMenuExpanded,
            liquidFabBackdrop = liquidFabBackdrop,
            fabMenu = fabMenu,
            modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding(),
        )
    }
}
