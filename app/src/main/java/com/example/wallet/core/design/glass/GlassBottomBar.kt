package com.example.wallet.core.design.glass

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** The bar's material: one flat color over the blurred [GlassBackdrop] behind it. Light mode
 * leans to a near-white grey, dark mode to near-black. Devices without blur (API < 31) get a
 * more opaque fill, since there the translucency alone has to carry the effect. */
private val LightBarColor = Color(0xFFEFEFF3)
private val DarkBarColor = Color(0xFF1A1A1E)

/** Kept low over the blur: the tint is there to soften and unify what shows through, not to hide
 * it. Too much and the frosted look flattens into a plain white/grey panel. */
private const val BlurredBarOpacity = 0.40f

/** Without blur to diffuse the content there's nothing to soften it, so the tint has to carry
 * legibility on its own and runs much heavier. */
private const val FlatBarOpacity = 0.92f

/**
 * liquid_glass_implementation_plan.md §16 — glass backdrop for the bottom navigation bar,
 * rendered as a fully-rounded floating pill (inset from the screen edges by its caller) rather
 * than a bar flush against the bottom edge, per the liquid-glass reference design. See
 * `BottomNavigation.kt` for the nav row/FAB content this wraps.
 *
 * Deliberately casts no shadow: the fill is translucent, so a drop shadow shows *through* the
 * bar and muddies it (and Android's shadow leaves an uneven band where it meets the outline).
 */
@Composable
fun GlassBottomBar(
    modifier: Modifier = Modifier,
    backdrop: GlassBackdrop? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    val barColor = if (isSystemInDarkTheme()) DarkBarColor else LightBarColor
    val opacity = if (backdrop != null && GlassCapabilities.supportsAdvancedBlur()) {
        BlurredBarOpacity
    } else {
        FlatBarOpacity
    }
    GlassSurface(
        modifier = modifier.fillMaxWidth(),
        style = GlassStyle.Regular,
        shape = GlassShapes.large,
        fill = barColor.copy(alpha = opacity),
        backdrop = backdrop,
        elevation = 0.dp,
        content = content,
    )
}
