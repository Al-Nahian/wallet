package com.expensetracker.wallet.core.design.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.expensetracker.wallet.core.design.glass.GlassColors
import com.expensetracker.wallet.core.design.glass.GlassShapes
import com.expensetracker.wallet.core.design.glass.GlassStyle

/**
 * Material3's [DropdownMenu] draws its own opaque `MaterialTheme.colorScheme.surfaceContainer`
 * rectangle with no color/shape override of its own — which reads as a flat, dull box next to
 * every other frosted-glass surface in this app. Rather than reimplementing anchored-popup
 * positioning (fiddly to get right), this wraps the real [DropdownMenu] in a scoped
 * [MaterialTheme] override so its *own* internal `Surface` picks up a glass-tinted fill and
 * rounded shape — anchoring, dismiss-on-outside-tap, and a11y all stay Material3's own.
 */
@Composable
fun GlassDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    tint: Color,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val glassFill = GlassColors.fill(GlassStyle.Thick, tint)
    val glassScheme = MaterialTheme.colorScheme.copy(
        surfaceContainer = glassFill,
        surface = glassFill,
    )
    MaterialTheme(colorScheme = glassScheme, typography = MaterialTheme.typography) {
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = onDismissRequest,
            modifier = modifier,
            shape = GlassShapes.medium,
            content = content,
        )
    }
}
