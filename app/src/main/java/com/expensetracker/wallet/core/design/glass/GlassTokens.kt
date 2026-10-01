package com.expensetracker.wallet.core.design.glass

import androidx.compose.ui.unit.dp

/**
 * Centralized visual constants for the glass material system (liquid_glass_implementation_plan.md
 * §6). Nothing outside `core/design/glass` should hardcode a glass alpha, blur radius or corner
 * radius — feature code only ever references [GlassStyle]/[GlassSurface] and friends, never these
 * raw numbers directly.
 */
object GlassTokens {
    val cornerSmall = 14.dp
    val cornerMedium = 20.dp
    val cornerLarge = 28.dp

    val borderWidth = 1.dp

    /** Self-blur radius applied to the material's own background fill on API 31+ devices
     * ([GlassCapabilities.supportsAdvancedBlur]) — see GlassSurface.kt for why this isn't a true
     * backdrop blur of content behind the surface. */
    val blurSmall = 12.dp
    val blurMedium = 20.dp
    val blurLarge = 32.dp

    val shadowElevation = 8.dp
    val shadowElevationStrong = 14.dp

    val regularAlpha = 0.72f
    val clearAlpha = 0.45f
    val thickAlpha = 0.84f
    val thinAlpha = 0.30f
    val vividAlpha = 0.90f

    /** Fill alpha for a "frosted" headline card (account/balance/stat/budget tiles) — translucent
     * enough to read as glass, but high enough that the tint stays visibly saturated and any
     * white text drawn over it keeps enough contrast. Lower (e.g. the ~0.55 this replaced) washes
     * the color to a dull pastel in light mode, which drags text legibility down with it. */
    val frostedFillAlpha = 0.75f

    /** How strongly [GlassStyle.Vivid]'s fill blends toward its [GlassSurface] `tint`, vs.
     * [contextTintAlpha] used by every other style's barely-there accent. */
    val vividTintBlend = 0.62f

    /** Colored glow cast by a tinted [GlassSurface] (`glow = true`) — an ambient/spot shadow tint
     * rather than a flat drop shadow, so the card reads as lit from within on a near-black page. */
    val glowAlpha = 0.55f
    val glowElevation = 20.dp

    /** Fallback alpha used instead of [regularAlpha]/etc. on devices below API 31, where there's
     * no blur to lean on — the material needs to read as "glass" from translucency + border +
     * highlight alone, so it runs a bit more opaque. */
    val fallbackAlphaBoost = 0.12f

    val highlightAlpha = 0.18f
    val borderAlpha = 0.20f
    val selectedTintAlpha = 0.14f
    val contextTintAlpha = 0.10f

    val pressedScale = 0.97f
    val interactionFastMs = 150
    val interactionNormalMs = 260
    val interactionSheetMs = 320
}
