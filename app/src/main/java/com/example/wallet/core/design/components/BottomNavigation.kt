package com.example.wallet.core.design.components

import android.os.Build
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow as TextGlowShadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.layout
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wallet.core.design.DarkPrimary
import com.example.wallet.core.design.NavActiveGreen
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
private val NavSelectedTint = NavActiveGreen
private val NavIdleTint = Color(0xFFAEB9C5)

/** Light mode's selected-tab tint — the theme's own [MaterialTheme.colorScheme.primary]
 * (`LightPrimary`, a dark sea green meant for text/button contrast on white) read as too dark
 * and heavy for a small icon+label against the near-white glass pill; this is a lighter, more
 * saturated mint that keeps the same hue family. */
private val NavSelectedTintLight = Color(0xFF2FBF71)

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
    // Drives the FAB's own "color drain" — its fill fades toward hollow glass and its "+"
    // rotates into an "×" in lockstep with fabMenu's expansion, as if the color were flowing out
    // of the FAB and into the fanned-out blobs.
    fabExpanded: Boolean = false,
    liquidFabBackdrop: LayerBackdrop? = null,
    // Rendered above the FAB, given its exact size (and this same liquidFabBackdrop, so its own
    // circles can render the identical real glass material the FAB uses) so a fan-out menu
    // (TemplateFabMenu) can anchor its blobs to it. Optional — only Home passes one.
    fabMenu: (@Composable (fabSize: Dp, liquidFabBackdrop: LayerBackdrop?) -> Unit)? = null,
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
                    NavItem(
                        item = item,
                        selected = item.route == selectedRoute,
                        onClick = { onItemSelected(item.route) },
                        modifier = Modifier.weight(1f),
                    )
                }
                // Reserves the center gap the popped-out FAB floats over, so the side items
                // stay evenly spaced instead of drifting toward the middle.
                Spacer(modifier = Modifier.size(fabSize))
                items.drop(midpoint).forEach { item ->
                    NavItem(
                        item = item,
                        selected = item.route == selectedRoute,
                        onClick = { onItemSelected(item.route) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
        // overlayAboveFab measures this at its own natural (fixed) size with no constraint from
        // this pill's small height, then places it directly above the FAB — without it, a plain
        // child this tall would either inflate this whole composable's measured height (a
        // regular Box sizes itself to fit its tallest child, dragging the visible bar up the
        // screen with it) or get starved down to near-zero height by this pill's own small
        // bounds (silently collapsing the menu's icons/labels to nothing, an earlier bug here).
        // Drawn BEFORE CenterFabItem (so the FAB paints on top of it): the fan's anchor blob
        // sits at the exact same position/size as the FAB to bridge the goo, but it's a flat
        // color with none of the FAB's real backdrop-glass rendering — if it painted on top, it
        // would flatten/hide the FAB's own look (and its rotating icon) the moment the menu
        // opened. With the FAB on top, its normal idle appearance is never covered by anything.
        if (fabMenu != null) {
            Box(modifier = Modifier.overlayAboveFab(fabSize)) {
                fabMenu(fabSize, liquidFabBackdrop)
            }
        }
        // Popped out above the bar's top edge, per the liquid-glass reference design — not
        // inline with the other nav items. Declared last so it paints on top of fabMenu's anchor
        // blob above.
        CenterFabItem(
            onClick = fabOnClick,
            contentDescription = fabContentDescription,
            size = fabSize,
            expanded = fabExpanded,
            liquidFabBackdrop = liquidFabBackdrop,
            modifier = Modifier.offset(y = (-fabSize / 6)),
        )
    }
}

/** Measures its content unconstrained (so a fixed-size overlay like [TemplateFabMenu] always
 * gets its full requested size, never starved down by this small pill's own bounds), reports a
 * zero footprint to ITS OWN parent (so it can't inflate that parent's measured size the way an
 * ordinary same-size Box child would), and places the real content's bottom-center directly
 * above [fabSize]'s own popped-out position — the same `-fabSize / 6` upward shift
 * [CenterFabItem] itself uses, so [TemplateFabMenu]'s internal "fan out from the FAB" math lines
 * up for free. */
private fun Modifier.overlayAboveFab(fabSize: Dp): Modifier = layout { measurable, _ ->
    val placeable = measurable.measure(Constraints())
    val fabSizePx = fabSize.roundToPx()
    // This layout node itself gets positioned at (parentCenterX, parentTop) by the outer Box's
    // TopCenter alignment (a zero-size node still receives that anchor point) — placement below
    // is relative to that. CenterFabItem's own bottom edge, in the same outer-box-local
    // coordinates, sits at (-fabSizePx/6 + fabSizePx) = 5*fabSizePx/6 down from parentTop.
    layout(0, 0) {
        placeable.place(
            x = -placeable.width / 2,
            y = (5 * fabSizePx / 6) - placeable.height,
        )
    }
}

/** PROTOTYPE: on API 31+ with [liquidBackdrop] supplied, the selected tab's icon sits on a small
 * real liquid-glass badge (io.github.kyant0:backdrop) — same safe capture pattern as the FAB
 */
@Composable
private fun NavItem(item: WalletBottomNavItem, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    // Light mode's pill is a near-white glass, so its tints must be the theme's darker
    // values (sea green / dark grey); dark mode keeps the pale pair that reads on slate.
    val tint = when {
        isSystemInDarkTheme() -> if (selected) NavSelectedTint else NavIdleTint
        selected -> NavSelectedTintLight
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .semantics {
                role = Role.Tab
                contentDescription = item.label
            }
            .padding(horizontal = 2.dp, vertical = 2.dp),
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = null,
            tint = tint,
            modifier = if (selected) {
                Modifier.drawBehind {
                    // A small, even halo hugging the glyph — kept tight (just a few dp past the
                    // icon's own radius) so it can't get clipped unevenly by the bar's bounds,
                    // which read as a lopsided glow when the radius was large.
                    val glowRadius = size.maxDimension / 2f + 4.dp.toPx()
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(tint.copy(alpha = 0.4f), Color.Transparent),
                            radius = glowRadius,
                            center = center,
                        ),
                        radius = glowRadius,
                        center = center,
                    )
                }
            } else {
                Modifier
            },
        )
        Text(
            text = item.label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                shadow = if (selected) {
                    TextGlowShadow(color = tint.copy(alpha = 0.6f), blurRadius = 6f)
                } else {
                    null
                },
            ),
            color = tint,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Clip,
        )
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
    expanded: Boolean = false,
) {
    val interactionSource = remember { MutableInteractionSource() }
    // Fixed mint green in both themes — the light-mode pill turned back to white glass, but
    // the plus icon stays green (the blue it once used in light mode was explicitly rejected);
    // a stable color also means it never shifts shade across a theme change.
    val tint = DarkPrimary

    // The "+" rotates 45° into an "×" — cheaper and smoother than crossfading two icons.
    val iconRotation by animateFloatAsState(
        targetValue = if (expanded) 45f else 0f,
        animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing),
        label = "fabIconRotation",
    )
    // A "color drain" on the FAB's own fill (fading it to hollow while the menu is open) was
    // tried three times here — tied to raw linear drainProgress, then re-eased through the
    // leftmost blob's own window, then a plain linear rescale of that same window — and every
    // version eventually left the FAB permanently stuck fully transparent after a collapse, in a
    // way that resisted diagnosis (even a pure linear, curve-free rescale reproduced it, ruling
    // out an easing-curve edge case as the sole cause). Given a broken, permanently-hollow FAB is
    // far worse than a FAB that simply never drains, the fill is now always constant — matching
    // the FAB's own idle look at all times, same as [FabMenuIcon]'s own material. The blobs'
    // goo/blur animation and this icon's own rotation below still carry the "something fluid is
    // happening" read without depending on this fragile alpha path.

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
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.graphicsLayer { rotationZ = iconRotation },
            )
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
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.graphicsLayer { rotationZ = iconRotation },
                )
            }
        }
    }
}
