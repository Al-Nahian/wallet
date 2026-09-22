package com.example.wallet.core.design.glass

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape

/**
 * liquid_glass_implementation_plan.md §18 — glass backdrop for a persistent top toolbar.
 * Edge-to-edge rectangular material (no rounding — it's flush against the screen's top edge, so
 * rounding the top corners would look wrong, same reasoning as iOS's edge-to-edge nav bars).
 * Wrap the real `TopAppBar`/`CenterAlignedTopAppBar` inside with its `containerColor` set
 * transparent so this surface's material shows through — see `TopBar.kt`.
 */
@Composable
fun GlassTopBar(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    GlassSurface(
        modifier = modifier.fillMaxWidth(),
        style = GlassStyle.Regular,
        shape = RectangleShape,
        elevation = GlassTokens.shadowElevation,
        content = content,
    )
}
