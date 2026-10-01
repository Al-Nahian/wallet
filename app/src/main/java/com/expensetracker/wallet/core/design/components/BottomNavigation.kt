package com.expensetracker.wallet.core.design.components

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
import com.expensetracker.wallet.core.design.DarkPrimary
import com.expensetracker.wallet.core.design.NavActiveGreen
import com.expensetracker.wallet.core.design.glass.GlassBottomBar
import com.expensetracker.wallet.core.design.glass.GlassCapabilities
import com.expensetracker.wallet.core.design.glass.GlassTokens
import com.expensetracker.wallet.core.design.glass.NotchedBottomBarShape
import com.expensetracker.wallet.core.design.glass.GlassInteraction
import com.expensetracker.wallet.core.design.glass.GlassStyle
import com.expensetracker.wallet.core.design.glass.GlassSurface
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
    // How far up from the bar's own (unnotched) top edge the FAB's center sits — the reference
    // image shows the FAB sitting almost entirely INSIDE the pocket, just barely breaking the
    // flat top edge rather than poking half-out of it, with a visible colored margin below it
    // before the pocket's own bottom curve. Shared between the FAB's own offset and
    // overlayAboveFab's math below so they can't drift apart.
    val fabRiseFraction = 0.35f
    // The bar's top edge dips into a deep, wide "bucket" pocket under the FAB instead of sitting
    // flush behind it — see NotchedBottomBarShape's own doc.
    val notchShape = remember(fabSize) {
        NotchedBottomBarShape(
            cornerRadius = GlassTokens.cornerLarge,
            notchWidth = fabSize * 1.5f,
            notchDepthFraction = 0.62f,
        )
    }
    Box(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 12.dp).fillMaxWidth(),
        contentAlignment = Alignment.TopCenter,
    ) {
        GlassBottomBar(modifier = Modifier.fillMaxWidth(), liquidBackdrop = liquidFabBackdrop, shape = notchShape) {
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
            Box(modifier = Modifier.overlayAboveFab(fabSize, fabRiseFraction)) {
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
            modifier = Modifier.offset(y = (-fabSize * fabRiseFraction)),
        )
    }
}

/** Measures its content unconstrained (so a fixed-size overlay like [TemplateFabMenu] always
 * gets its full requested size, never starved down by this small pill's own bounds), reports a
 * zero footprint to ITS OWN parent (so it can't inflate that parent's measured size the way an
 * ordinary same-size Box child would), and places the real content's bottom-center directly
 * above [fabSize]'s own popped-out position — the same `-fabSize * riseFraction` upward shift
 * [CenterFabItem] itself uses, so [TemplateFabMenu]'s internal "fan out from the FAB" math lines
 * up for free. */
private fun Modifier.overlayAboveFab(fabSize: Dp, riseFraction: Float): Modifier = layout { measurable, _ ->
    val placeable = measurable.measure(Constraints())
    val fabSizePx = fabSize.roundToPx()
    // This layout node itself gets positioned at (parentCenterX, parentTop) by the outer Box's
    // TopCenter alignment (a zero-size node still receives that anchor point) — placement below
    // is relative to that. CenterFabItem's own bottom edge, in the same outer-box-local
    // coordinates, sits at (-fabSizePx*riseFraction + fabSizePx) down from parentTop.
    layout(0, 0) {
        placeable.place(
            x = -placeable.width / 2,
            y = (fabSizePx * (1f - riseFraction)).toInt() - placeable.height,
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
    // Frame-by-frame extraction of the reference's own fluid-ref.mp4 (see references/) shows the
    // FAB and every fanned-out circle staying fully solid/opaque pink for the entire open and
    // close motion — there's no fade, hollow, or transparency change anywhere. The "liquid" read
    // comes entirely from shape: a new circle visibly buds out of the FAB's edge as a swollen
    // bump joined by a thick neck, the neck thins as it travels outward, then cleanly pinches off
    // — a metaball melt, not an alpha animation. Several earlier attempts here tried draining the
    // FAB's own fill to hollow while the menu was open; besides never matching the reference, one
    // version got stuck permanently transparent after a collapse. The fill is simply constant.

    if (liquidFabBackdrop != null && GlassCapabilities.supportsAdvancedBlur()) {
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
                    // A faint, subtle rim rather than the earlier crisp near-opaque ring — that
                    // combined with vibrancy() read as a bright cyan/white neon halo around the
                    // button against the reference's plain, calm FAB.
                    highlight = { Highlight(width = 1.5.dp, alpha = 0.35f, style = HighlightStyle.Default(intensity = 0.4f)) },
                    // A plain soft drop shadow, not a colored glow — a wide, tinted, high-alpha
                    // shadow read as a neon halo around the button instead of the reference's
                    // subtle elevation shade.
                    shadow = { Shadow(radius = 10.dp, color = Color.Black.copy(alpha = 0.25f)) },
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
