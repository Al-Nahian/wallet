package com.example.wallet.core.design.glass

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * liquid_glass_implementation_plan.md §16 — glass backdrop for the bottom navigation bar,
 * rendered as a fully-rounded floating pill (inset from the screen edges by its caller) rather
 * than a bar flush against the bottom edge, per the liquid-glass reference design. See
 * `BottomNavigation.kt` for the nav row/FAB content this wraps.
 */
@Composable
fun GlassBottomBar(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    GlassSurface(
        modifier = modifier.fillMaxWidth(),
        style = GlassStyle.Regular,
        shape = GlassShapes.large,
        elevation = GlassTokens.shadowElevationStrong,
        content = content,
    )
}
