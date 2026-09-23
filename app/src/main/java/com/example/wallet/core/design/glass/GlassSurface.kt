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
 * **Honest scope note**: true "backdrop" blur — blurring the live app content sitting behind this
 * surface in z-order, the way iOS Liquid Glass does — needs either capturing the layer behind
 * this one every frame (an advanced Compose 1.7 `GraphicsLayer`-record technique, expensive and
 * easy to get misaligned/janky across every screen) or an OS-level compositor hook Android
 * doesn't expose for in-content overlays (window-level `blurBehindRadius`, used by [GlassSheet]
 * for dialogs, only blurs what's behind an entire *window*, not a view within one). This surface
 * deliberately does not attempt that: it composes translucency + contextual tint + specular
 * highlight + thin border + soft shadow, which is the achievable, stable subset of the material
 * (plan §29's fallback rule already assumes blur may be unavailable). [GlassSheet] is where real
 * platform blur-behind is used, because dialogs get their own window.
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
    /** When true and [tint] is set, casts a colored ambient/spot shadow instead of a flat black
     * one, so the surface reads as lit from within against a near-black page. Reserved for
     * headline [GlassStyle.Vivid] cards — chrome surfaces (bars, sheets) never set this. */
    glow: Boolean = false,
    interaction: GlassInteraction = GlassInteraction.None,
    interactionSource: MutableInteractionSource? = null,
    enabled: Boolean = true,
    elevation: Dp = GlassTokens.shadowElevation,
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

    Box(
        // Shadow must be the outermost modifier: it draws unclipped, extending past the box's
        // layout bounds. If it sat inside the graphicsLayer's Offscreen-composited buffer below
        // (sized to the layout bounds, for the translucent-layer blending that buffer exists
        // for), its soft blur would get hard-cut into a visible rectangle — the bug this order
        // avoids.
        modifier = modifier
            .let { base ->
                if (glow && tint != null) {
                    base.shadow(
                        elevation = if (enabled) elevation else 0.dp,
                        shape = shape,
                        clip = false,
                        ambientColor = tint.copy(alpha = GlassTokens.glowAlpha),
                        spotColor = tint.copy(alpha = GlassTokens.glowAlpha),
                    )
                } else {
                    base.shadow(elevation = if (enabled) elevation else 0.dp, shape = shape, clip = false)
                }
            }
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                compositingStrategy = CompositingStrategy.Offscreen
            }
            .clip(shape),
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
