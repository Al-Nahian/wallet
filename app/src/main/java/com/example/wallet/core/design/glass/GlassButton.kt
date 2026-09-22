package com.example.wallet.core.design.glass

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * liquid_glass_implementation_plan.md §4 — a clickable glass pill for chrome-level actions
 * (contextual controls, filter/segment toggles), not a replacement for the app's normal
 * content-level `PrimaryButton`/`SecondaryButton` (plan §1: "Content is primary. Glass is
 * functional chrome" — most in-form buttons stay normal surfaces).
 */
@Composable
fun GlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tint: Color? = null,
    style: GlassStyle = GlassStyle.Regular,
) {
    val interactionSource = remember { MutableInteractionSource() }
    GlassSurface(
        modifier = modifier
            .defaultMinSize(minHeight = 48.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick,
            ),
        style = style,
        shape = GlassShapes.small,
        tint = tint,
        interaction = GlassInteraction.Pressable,
        interactionSource = interactionSource,
        enabled = enabled,
    ) {
        Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp), contentAlignment = Alignment.Center) {
            CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurface) {
                Text(text = text, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}
