package com.example.wallet.core.design.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Notifications
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Extra inset added on top of [CenterAlignedTopAppBar]'s own ~4dp nav-icon/action padding, so the
 * profile/bell glass bubbles' outer edges land on the same 16dp margin every screen's card list
 * uses below — without this the bubbles sit noticeably closer to the screen edge than the cards. */
private val TopBarIconExtraInset = 12.dp

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
            LiquidCircleIconButton(
                onClick = onProfileClick,
                contentDescription = "Profile",
                modifier = Modifier.padding(start = TopBarIconExtraInset),
                size = TopBarBubbleSize,
            ) {
                // Reference design's top-left control is a solid person silhouette (head +
                // shoulders), not a ringed account-circle glyph. White in dark mode, slate
                // navy in light mode where a white glyph would vanish on the bright bead.
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = null,
                    tint = topBarGlyphTint(),
                    modifier = Modifier.size(topBarGlyphSize()),
                )
            }
        },
        actions = {
            LiquidCircleIconButton(
                onClick = onNotificationsClick,
                contentDescription = notificationContentDescription(unreadNotificationCount),
                modifier = Modifier.padding(end = TopBarIconExtraInset),
                size = TopBarBubbleSize,
            ) {
                NotificationBellIcon(unreadCount = unreadNotificationCount, glyphSize = topBarGlyphSize())
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            scrolledContainerColor = Color.Transparent,
        ),
    )
}

/** Diameter of the top-bar profile/notification glass bubbles, matching the reference
 * design's chrome — noticeably larger than the 40dp default [LiquidCircleIconButton] ships
 * with. Glyphs inside run just over half the bubble. Light mode's glyphs run a touch larger,
 * matching its chunkier white beads; dark keeps its confirmed size. */
private val TopBarBubbleSize = 40.dp
private val TopBarGlyphSizeLight = 24.dp
private val TopBarGlyphSizeDark = 22.dp

@Composable
private fun topBarGlyphSize(): Dp =
    if (isSystemInDarkTheme()) TopBarGlyphSizeDark else TopBarGlyphSizeLight

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
private fun NotificationBellIcon(unreadCount: Int, glyphSize: Dp) {
    // Reference design's bell is a thin outline glyph, not the filled one. Same dark/light
    // glyph-tint treatment as the profile icon.
    val bellIcon: ImageVector = Icons.Outlined.Notifications
    val glyphTint = topBarGlyphTint()
    if (unreadCount <= 0) {
        Icon(
            imageVector = bellIcon,
            contentDescription = null,
            tint = glyphTint,
            modifier = Modifier.size(glyphSize),
        )
        return
    }
    BadgedBox(
        badge = {
            Badge(containerColor = MaterialTheme.colorScheme.error) {
                Text(if (unreadCount > 99) "99+" else unreadCount.toString())
            }
        },
    ) {
        Icon(
            imageVector = bellIcon,
            contentDescription = null,
            tint = glyphTint,
            modifier = Modifier.size(glyphSize),
        )
    }
}

/** Glyph color for the top-bar bubbles: white on the dark steel-blue bead, slate navy on the
 * bright-white light-mode bead (reference dashboard) — black read as harsh against the white
 * glass, while slate matches the reference's person/bell glyphs. */
@Composable
private fun topBarGlyphTint(): Color =
    if (isSystemInDarkTheme()) Color.White else Color(0xFF3B4D6B)
