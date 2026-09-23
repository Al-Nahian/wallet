package com.example.wallet.core.design.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.wallet.core.design.glass.GlassBackdrop
import com.example.wallet.core.design.glass.GlassBottomBar
import com.example.wallet.core.design.glass.GlassInteraction
import com.example.wallet.core.design.glass.GlassStyle
import com.example.wallet.core.design.glass.GlassSurface

data class WalletBottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
)

/** Vertical space the floating nav pill occupies. Scrollable screens add this to their bottom
 * content padding so their last item can scroll clear of the bar — the bar overlays content
 * (rather than reserving opaque layout space) so content stays visible through its glass. */
val WalletBottomNavSpace = 100.dp

/** Floating glass pill nav bar with the primary Add action embedded as a glowing center button
 * between the second and third destinations, per the liquid-glass reference design — not a
 * separate `Scaffold` FAB floating above the bar. */
@Composable
fun WalletBottomNavigation(
    items: List<WalletBottomNavItem>,
    selectedRoute: String,
    onItemSelected: (String) -> Unit,
    fabOnClick: () -> Unit,
    fabContentDescription: String,
    modifier: Modifier = Modifier,
    backdrop: GlassBackdrop? = null,
) {
    val midpoint = items.size / 2
    val fabSize = 60.dp
    Box(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 12.dp).fillMaxWidth(),
        contentAlignment = Alignment.TopCenter,
    ) {
        GlassBottomBar(modifier = Modifier.fillMaxWidth(), backdrop = backdrop) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                items.take(midpoint).forEach { item ->
                    NavItem(item = item, selected = item.route == selectedRoute, onClick = { onItemSelected(item.route) })
                }
                // Reserves the center gap the popped-out FAB floats over, so the side items
                // stay evenly spaced instead of drifting toward the middle.
                Spacer(modifier = Modifier.size(fabSize))
                items.drop(midpoint).forEach { item ->
                    NavItem(item = item, selected = item.route == selectedRoute, onClick = { onItemSelected(item.route) })
                }
            }
        }
        // Popped out above the bar's top edge, per the liquid-glass reference design — not
        // inline with the other nav items.
        CenterFabItem(
            onClick = fabOnClick,
            contentDescription = fabContentDescription,
            size = fabSize,
            backdrop = backdrop,
            modifier = Modifier.offset(y = (-fabSize / 6)),
        )
    }
}

@Composable
private fun NavItem(item: WalletBottomNavItem, selected: Boolean, onClick: () -> Unit) {
    val tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .semantics {
                role = Role.Tab
                contentDescription = item.label
            }
            .padding(horizontal = 4.dp, vertical = 2.dp),
    ) {
        Icon(imageVector = item.icon, contentDescription = null, tint = tint)
        Text(text = item.label, style = MaterialTheme.typography.labelSmall, color = tint)
    }
}

/** Blurs the content behind it like the bar does — the top of the button pops out past the bar,
 * so without it page text reads straight through that sliver. */
@Composable
private fun CenterFabItem(
    onClick: () -> Unit,
    contentDescription: String,
    size: Dp,
    backdrop: GlassBackdrop?,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    GlassSurface(
        modifier = modifier
            .size(size)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .semantics {
                role = Role.Button
                this.contentDescription = contentDescription
            },
        style = GlassStyle.Vivid,
        shape = CircleShape,
        tint = MaterialTheme.colorScheme.primary,
        backdrop = backdrop,
        glow = true,
        interaction = GlassInteraction.Pressable,
        interactionSource = interactionSource,
        elevation = 12.dp,
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Icon(imageVector = Icons.Filled.Add, contentDescription = null, tint = Color.White)
        }
    }
}
