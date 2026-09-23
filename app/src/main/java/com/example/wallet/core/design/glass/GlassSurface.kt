package com.example.wallet.core.design.glass

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * liquid_glass_implementation_plan.md §4/§10 — the one primitive every other Glass* component is
 * built on. Feature code should reach for [GlassButton]/[GlassBottomBar]/[GlassTopBar]/[GlassFab]/
 * [GlassSheet] first; this is the escape hatch for a one-off glass region.
 *
 * Rendering pipeline (plan §7): shadow → clip → translucent material fill → specular highlight →
 * thin border → content.
 *
 * True "backdrop" blur — blurring the live content sitting behind the surface in z-order, the way
 * iOS Liquid Glass does — is available by passing a [backdrop] (see [GlassBackdrop]), which costs
 * a per-frame `GraphicsLayer` recording of the content being blurred; the bottom nav bar opts in,
 * most surfaces don't need to. Without it the surface composes translucency + contextual tint +
 * specular highlight + thin border + soft shadow, which reads as glass on its own (plan §29's
 * fallback rule already assumes blur may be unavailable, as it is below API 31). [GlassSheet]
 * uses real platform window blur-behind instead, because dialogs get their own window.
 */
@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    style: GlassStyle = GlassStyle.Regular,
    shape: Shape = GlassShapes.medium,
    tint: Color? = null,
    /** Replaces the [style]/[tint]-derived material fill with this exact (usually partly
     * transparent) color. For the rare surface that needs a specific solid color at a specific
     * opacity rather than the computed glass blend — see [GlassBottomBar]. */
    fill: Color? = null,
    /** Content recorded behind this surface, drawn back blurred underneath its fill — a real
     * backdrop blur (API 31+). See [GlassBackdrop]. */
    backdrop: GlassBackdrop? = null,
    /** When true and [tint] is set, casts a colored ambient/spot shadow instead of a flat black
     * one, so the surface reads as lit from within against a near-black page. Reserved for
     * headline [GlassStyle.Vivid] cards — chrome surfaces (bars, sheets) never set this. */
    glow: Boolean = false,
    interaction: GlassInteraction = GlassInteraction.None,
    interactionSource: MutableInteractionSource? = null,
    enabled: Boolean = true,
    elevation: Dp = GlassTokens.shadowElevation,
    /** wallet_app_stability_performance_plan.md §17-20's STATIC tier: skips the Offscreen
     * compositing layer this surface would otherwise always allocate, the dominant per-frame GPU
     * cost when many instances are visible at once — a scrolling list of rows being exactly that
     * case. Trades a very slightly less "grouped" translucency blend (imperceptible for a plain
     * fill + a faint top highlight, the only two layers a non-[glow] surface draws) for a real
     * scroll-performance win. Leave `false` (the default) for chrome, dialogs, and cards shown a
     * handful at a time, where the cost is negligible; set `true` for any [GlassSurface] used as
     * a row inside a scrolling `LazyColumn`/`LazyRow`. */
    lightweight: Boolean = false,
    content: @Composable BoxScope.() -> Unit,
) {
    val motion = LocalGlassMotionPreferences.current
    val fillColor = fill ?: GlassColors.fill(style, tint, motion)
    val highlightBrush = GlassColors.highlightBrush()
    val borderBrush = GlassColors.borderBrush(tint = if (glow) tint else null)

    val isPressed by (interactionSource?.collectIsPressedAsState() ?: remember { mutableStateOf(false) })
    val reactsToPress = interaction == GlassInteraction.Pressable && enabled
    val pressAnimationSpec = if (motion.reduceMotion) tween<Float>(0) else tween(GlassTokens.interactionFastMs)
    val scale by animateFloatAsState(
        targetValue = if (reactsToPress && isPressed) GlassTokens.pressedScale else 1f,
        animationSpec = pressAnimationSpec,
        label = "glassPressScale",
    )
    val pressTintBoost = if (reactsToPress && isPressed) GlassTokens.selectedTintAlpha else 0f

    val effectiveElevation = if (enabled) elevation else 0.dp

    Box(
        // Shadow must be the outermost modifier: it draws unclipped, extending past the box's
        // layout bounds. If it sat inside the graphicsLayer's Offscreen-composited buffer below
        // (sized to the layout bounds, for the translucent-layer blending that buffer exists
        // for), its soft blur would get hard-cut into a visible rectangle — the bug this order
        // avoids. Skipped entirely at zero elevation (every plain list row: [lightweight]
        // surfaces all pass `elevation = 0.dp`) rather than calling `shadow(elevation = 0.dp)`
        // and trusting it to be a no-op — `Modifier.shadow` is itself backed by a graphicsLayer,
        // and chaining it straight into `clip(shape)` with no compositing boundary between them
        // left a faint rectangular seam along the bottom edge on a pure-black background, most
        // visible on exactly these zero-elevation rows.
        modifier = modifier
            .let { base ->
                if (effectiveElevation == 0.dp) {
                    base
                } else if (glow && tint != null) {
                    base.shadow(
                        elevation = effectiveElevation,
                        shape = shape,
                        clip = false,
                        ambientColor = tint.copy(alpha = GlassTokens.glowAlpha),
                        spotColor = tint.copy(alpha = GlassTokens.glowAlpha),
                    )
                } else {
                    base.shadow(elevation = effectiveElevation, shape = shape, clip = false)
                }
            }
            .let { base ->
                when {
                    !lightweight -> base.graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        compositingStrategy = CompositingStrategy.Offscreen
                    }
                    reactsToPress -> base.graphicsLayer { scaleX = scale; scaleY = scale }
                    else -> base
                }
            }
            .clip(shape)
            .glassBackdrop(backdrop),
    ) {
        // Translucent material fill.
        Box(modifier = Modifier.matchParentSize().background(fillColor))
        if (pressTintBoost > 0f) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = pressTintBoost)),
            )
        }
        // Specular highlight — top edge only, barely visible.
        Box(modifier = Modifier.matchParentSize().background(highlightBrush))
        // Thin border — only drawn for glowing tinted (Vivid) cards, where it reads as the
        // colored glow's rim. Chrome surfaces (bars, sheets, plain rows) skip it: a neutral
        // 1dp border on a large/edge-to-edge shape reads as an unwanted outline box rather than
        // a material edge.
        if (glow && tint != null) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .border(width = GlassTokens.borderWidth * 1.5f, brush = borderBrush, shape = shape),
            )
        }
        content()
    }
}

/** Convenience for call sites that just want the motion-preference-aware press animation spec
 * without the full surface (e.g. an icon nudge alongside a [GlassSurface]). */
@Composable
fun glassInteractionSpec() =
    if (LocalGlassMotionPreferences.current.reduceMotion) tween<Float>(0) else tween<Float>(GlassTokens.interactionFastMs)

@Composable
fun ProvideGlassMotionPreferences(preferences: GlassMotionPreferences, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalGlassMotionPreferences provides preferences, content = content)
}
