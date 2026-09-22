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

    /** The material's translucent fill: base surface color blended with [tint] (plan §13 — "base
     * glass + contextual accent"). Every style except [GlassStyle.Vivid] uses a barely-there
     * accent blend. [GlassStyle.Vivid] blends almost entirely to the tint itself — not toward
     * [MaterialTheme]'s surface color — so a headline card reads as the same saturated color in
     * both light and dark theme (blending toward a near-white light-theme surface would wash the
     * tint out to a pale pastel, breaking contrast with the card's white text/icons). */
    @Composable
    fun fill(style: GlassStyle, tint: Color?, motion: GlassMotionPreferences = LocalGlassMotionPreferences.current): Color {
        val base = MaterialTheme.colorScheme.surface
        val blendStrength = if (style == GlassStyle.Vivid) GlassTokens.vividTintBlend else GlassTokens.contextTintAlpha
        val blended = tint?.let {
            if (style == GlassStyle.Vivid) it else lerp(base, it, blendStrength)
        } ?: base
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
            GlassStyle.Vivid -> GlassTokens.vividAlpha
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
     * (plan §30, "the background should influence the perceived material in both modes").
     * When [tint] is given (a [GlassStyle.Vivid] card), the edge leans toward a brighter version
     * of the tint instead of plain white/black, so the border reads as a colored glow rim. */
    @Composable
    fun borderBrush(tint: Color? = null): Brush {
        val isDark = isSystemInDarkTheme()
        if (tint != null) {
            val bright = lerp(tint, Color.White, 0.45f)
            return Brush.verticalGradient(
                listOf(
                    bright.copy(alpha = GlassTokens.vividTintBlend + 0.1f),
                    tint.copy(alpha = GlassTokens.vividTintBlend * 0.5f),
                ),
            )
        }
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
