package com.example.wallet.core.design.glass

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

/**
 * liquid_glass_implementation_plan.md §6/§11/§12/§13/§30 — resolves the actual colors/brushes a
 * [GlassSurface] paints, given a [GlassStyle], an optional contextual [tint] and the current
 * capability/accessibility state. This is the only place style→alpha/color math happens; nothing
 * else in the app should compute a glass alpha by hand.
 */
object GlassColors {

    /** The material's translucent fill: base surface color blended with a low-alpha contextual
     * [tint] (plan §13 — "base glass + contextual accent", never a saturated rainbow fill). */
    @Composable
    fun fill(style: GlassStyle, tint: Color?, motion: GlassMotionPreferences = LocalGlassMotionPreferences.current): Color {
        val base = MaterialTheme.colorScheme.surface
        val blended = tint?.let { lerp(base, it, GlassTokens.contextTintAlpha) } ?: base
        return blended.copy(alpha = resolveAlpha(style, motion))
    }

    /** liquid_glass_implementation_plan.md §29 — devices without blur (or with reduced
     * transparency requested) get a more opaque fill instead of relying on backdrop blur they
     * don't have. */
    fun resolveAlpha(style: GlassStyle, motion: GlassMotionPreferences): Float {
        val baseAlpha = when (style) {
            GlassStyle.Regular, GlassStyle.Interactive -> GlassTokens.regularAlpha
            GlassStyle.Clear -> GlassTokens.clearAlpha
            GlassStyle.Thick -> GlassTokens.thickAlpha
            GlassStyle.Thin -> GlassTokens.thinAlpha
        }
        val needsBoost = motion.reduceTransparency || !GlassCapabilities.supportsAdvancedBlur()
        return if (needsBoost) (baseAlpha + GlassTokens.fallbackAlphaBoost).coerceAtMost(0.96f) else baseAlpha
    }

    /** Soft specular highlight along the top edge (plan §11) — a barely-there gradient, not a
     * flat white outline. */
    @Composable
    fun highlightBrush(): Brush {
        val isDark = isSystemInDarkTheme()
        val edge = if (isDark) Color.White else Color.White
        val alpha = if (isDark) GlassTokens.highlightAlpha * 0.85f else GlassTokens.highlightAlpha
        return Brush.verticalGradient(
            listOf(
                edge.copy(alpha = alpha),
                edge.copy(alpha = alpha * 0.3f),
                Color.Transparent,
            ),
        )
    }

    /** Thin border brush (plan §12) — dark-mode borders lean toward a light edge, light-mode
     * borders lean toward a soft dark edge, so the material reads correctly in both modes
     * (plan §30, "the background should influence the perceived material in both modes"). */
    @Composable
    fun borderBrush(): Brush {
        val isDark = isSystemInDarkTheme()
        val edgeColor = if (isDark) Color.White else Color.Black
        return Brush.verticalGradient(
            listOf(
                edgeColor.copy(alpha = GlassTokens.borderAlpha),
                edgeColor.copy(alpha = GlassTokens.borderAlpha * 0.4f),
            ),
        )
    }

    @Composable
    fun selectedTint(tint: Color = MaterialTheme.colorScheme.primary): Color =
        tint.copy(alpha = GlassTokens.selectedTintAlpha)
}
