package com.expensetracker.wallet.core.design.glass

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape

/**
 * liquid_glass_implementation_plan.md §19 — the container for modal content (confirmation/input
 * dialogs, pickers). Pairs a strong [GlassStyle.Thick] [GlassSurface] with real window-level
 * blur-behind ([GlassWindowBlur]) so the dimmed background genuinely blurs, not just darkens.
 * Must be called from inside a `Dialog`'s content.
 */
@Composable
fun GlassSheet(
    modifier: Modifier = Modifier,
    shape: Shape = GlassShapes.large,
    content: @Composable BoxScope.() -> Unit,
) {
    GlassWindowBlur()
    GlassSurface(
        modifier = modifier,
        style = GlassStyle.Thick,
        shape = shape,
        elevation = GlassTokens.shadowElevationStrong,
        content = content,
    )
}

/** Bare backdrop-blur wiring for call sites that keep Material3's own `AlertDialog` chrome
 * (title/text/button slots, back-gesture and a11y handling all for free) but still want the
 * window behind them genuinely blurred and a glass-tinted container color — see
 * `ConfirmationDialog`/`TextInputDialog` for the pattern. */
@Composable
fun glassDialogContainerColor(style: GlassStyle = GlassStyle.Thick) = GlassColors.fill(style, tint = null)
