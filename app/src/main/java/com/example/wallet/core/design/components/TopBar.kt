package com.example.wallet.core.design.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.wallet.core.design.glass.GlassCircleIconButton

/**
 * Shared top bar for every top-level screen (plan.md §83): a profile icon on the left
 * (account/login entry point) and a notification bell on the right (in-app notification tray),
 * framing the screen title. Both [onProfileClick] and [onNotificationsClick] default to no-ops so
 * screens can adopt this bar before their destinations exist.
 *
 * Per the liquid-glass reference design, the bar itself carries no visible plate — only the
 * profile and notification controls are individually wrapped in glass circle bubbles, floating
 * directly on the page background.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletTopBar(
    title: String,
    modifier: Modifier = Modifier,
    unreadNotificationCount: Int = 0,
    onProfileClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
) {
    CenterAlignedTopAppBar(
        modifier = modifier,
        title = { Text(title) },
        navigationIcon = {
            GlassCircleIconButton(onClick = onProfileClick, contentDescription = "Profile") {
                Icon(imageVector = Icons.Filled.AccountCircle, contentDescription = null)
            }
        },
        actions = {
            GlassCircleIconButton(onClick = onNotificationsClick, contentDescription = notificationContentDescription(unreadNotificationCount)) {
                NotificationBellIcon(unreadCount = unreadNotificationCount)
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color.Transparent,
            scrolledContainerColor = Color.Transparent,
        ),
    )
}

private fun notificationContentDescription(unreadCount: Int): String =
    if (unreadCount > 0) "Notifications ($unreadCount unread)" else "Notifications"

/** Shared top bar for pushed back-button screens (account/budget/category/label forms and
 * detail screens, profile, notification center) — same transparent, no-plate treatment as
 * [WalletTopBar] (a full-width glass bar plate here reads as an unwanted outline box, since
 * unlike the bottom nav/FAB it isn't a floating chrome element), with the standard
 * back-arrow-left/title layout instead of the top-level profile+bell framing. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlassScreenTopBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
) {
    TopAppBar(
        modifier = modifier,
        title = { Text(title) },
        navigationIcon = navigationIcon,
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            scrolledContainerColor = Color.Transparent,
        ),
    )
}

@Composable
private fun NotificationBellIcon(unreadCount: Int) {
    val bellIcon: ImageVector = Icons.Filled.Notifications
    if (unreadCount <= 0) {
        Icon(imageVector = bellIcon, contentDescription = null)
        return
    }
    BadgedBox(
        badge = {
            Badge(containerColor = MaterialTheme.colorScheme.error) {
                Text(if (unreadCount > 99) "99+" else unreadCount.toString())
            }
        },
    ) {
        Icon(imageVector = bellIcon, contentDescription = null)
    }
}
