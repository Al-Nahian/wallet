package com.example.wallet.core.design.glass

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * liquid_glass_implementation_plan.md §16 — glass backdrop for the bottom navigation bar. Rounded
 * top edge only (it's flush against the bottom of the screen, so only the top edge reads as a
 * material boundary). Wrap the real `NavigationBar` inside with its `containerColor` set
 * transparent so this surface's material shows through — see `BottomNavigation.kt`.
 */
@Composable
fun GlassBottomBar(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    GlassSurface(
        modifier = modifier.fillMaxWidth(),
        style = GlassStyle.Regular,
        shape = GlassShapes.topRounded(),
        elevation = GlassTokens.shadowElevationStrong,
        content = content,
    )
}
