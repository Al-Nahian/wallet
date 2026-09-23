package com.example.wallet.core.design.glass

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/** The bar's material: one flat color at 60% opacity rather than the usual style-derived blend,
 * so whatever scrolls underneath stays visible through it — that translucency is the whole
 * effect. Light mode leans to a near-white grey, dark mode to near-black. */
private val LightBarColor = Color(0xFFE9E9EE)
private val DarkBarColor = Color(0xFF1A1A1E)
private const val BarOpacity = 0.60f

/**
 * liquid_glass_implementation_plan.md §16 — glass backdrop for the bottom navigation bar,
 * rendered as a fully-rounded floating pill (inset from the screen edges by its caller) rather
 * than a bar flush against the bottom edge, per the liquid-glass reference design. See
 * `BottomNavigation.kt` for the nav row/FAB content this wraps.
 */
@Composable
fun GlassBottomBar(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    val barColor = if (isSystemInDarkTheme()) DarkBarColor else LightBarColor
    GlassSurface(
        modifier = modifier.fillMaxWidth(),
        style = GlassStyle.Regular,
        shape = GlassShapes.large,
        fill = barColor.copy(alpha = BarOpacity),
        elevation = GlassTokens.shadowElevationStrong,
        content = content,
    )
}
