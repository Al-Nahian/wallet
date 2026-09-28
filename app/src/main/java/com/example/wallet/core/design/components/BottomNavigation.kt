package com.example.wallet.core.design.components

import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.wallet.core.design.DarkPrimary
import com.example.wallet.core.design.glass.GlassBottomBar
import com.example.wallet.core.design.glass.GlassInteraction
import com.example.wallet.core.design.glass.GlassStyle
import com.example.wallet.core.design.glass.GlassSurface
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.highlight.HighlightStyle
import com.kyant.backdrop.shadow.Shadow

/** Nav item tints on the dark mode slate pill: mint-green selected, cool grey idle. Light
 * mode's near-white liquid-glass pill instead falls back to the theme's own primary /
 * onSurfaceVariant (see [NavItem]) — these pale values would wash out against white. */
private val NavSelectedTint = DarkPrimary
private val NavIdleTint = Color(0xFFAEB9C5)

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
    liquidFabBackdrop: LayerBackdrop? = null,
) {
    val midpoint = items.size / 2
    val fabSize = 60.dp
    Box(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 12.dp).fillMaxWidth(),
        contentAlignment = Alignment.TopCenter,
    ) {
        GlassBottomBar(modifier = Modifier.fillMaxWidth(), liquidBackdrop = liquidFabBackdrop) {
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
            liquidFabBackdrop = liquidFabBackdrop,
            modifier = Modifier.offset(y = (-fabSize / 6)),
        )
    }
}

/** PROTOTYPE: on API 31+ with [liquidBackdrop] supplied, the selected tab's icon sits on a small
 * real liquid-glass badge (io.github.kyant0:backdrop) — same safe capture pattern as the FAB
 */
@Composable
private fun NavItem(item: WalletBottomNavItem, selected: Boolean, onClick: () -> Unit) {
    // Light mode's pill is a near-white glass, so its tints must be the theme's darker
    // values (sea green / dark grey); dark mode keeps the pale pair that reads on slate.
    val tint = when {
        isSystemInDarkTheme() -> if (selected) NavSelectedTint else NavIdleTint
        selected -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
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
 * so without it page text reads straight through that sliver.
 *
 * PROTOTYPE: on API 31+ with [liquidFabBackdrop] supplied, this renders with a real liquid-glass
 * refraction (io.github.kyant0:backdrop) instead of [GlassSurface]'s translucent-fill-only look —
 * the background genuinely bends/bulges behind the button, not just blurs. Below API 31, or if no
 * [liquidFabBackdrop] was captured, falls back to the existing [GlassSurface] rendering
 * unchanged. */
@Composable
private fun CenterFabItem(
    onClick: () -> Unit,
    contentDescription: String,
    size: Dp,
    liquidFabBackdrop: LayerBackdrop?,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    // Fixed mint green in both themes — the light-mode pill turned back to white glass, but
    // the plus icon stays green (the blue it once used in light mode was explicitly rejected);
    // a stable color also means it never shifts shade across a theme change.
    val tint = DarkPrimary
    if (liquidFabBackdrop != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        Box(
            modifier = modifier
                .size(size)
                .drawBackdrop(
                    backdrop = liquidFabBackdrop,
                    shape = { CircleShape },
                    effects = {
                        vibrancy()
                        blur(2.dp.toPx())
                        // lens() itself no-ops below API 33 (RuntimeShader requirement) —
                        // the button still gets a real blur, just no refraction distortion.
                        lens(12.dp.toPx(), 24.dp.toPx(), depthEffect = true)
                    },
                    // A crisp, near-opaque white ring rather than a soft highlight — the reference
                    // design's FAB reads as having a distinct white border, not just a glow.
                    highlight = { Highlight(width = 3.dp, alpha = 1f, style = HighlightStyle.Default(intensity = 1f)) },
                    shadow = { Shadow(radius = 16.dp, color = tint.copy(alpha = 0.55f)) },
                    onDrawSurface = {
                        drawRect(tint, blendMode = BlendMode.Hue)
                        drawRect(tint.copy(alpha = 0.75f))
                    },
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick,
                )
                .semantics {
                    role = Role.Button
                    this.contentDescription = contentDescription
                },
            contentAlignment = Alignment.Center,
        ) {
            Icon(imageVector = Icons.Filled.Add, contentDescription = null, tint = Color.White)
        }
    } else {
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
            tint = tint,
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
}
