package com.expensetracker.wallet.core.design.glass

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/**
 * liquid_glass_implementation_plan.md §17 — the central Add action as a visual focal point:
 * stronger [GlassStyle.Thick] material, press scale, ≥48dp touch target. Not an unmodified
 * Material `FloatingActionButton`.
 */
@Composable
fun GlassFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    content: @Composable () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    GlassSurface(
        modifier = modifier
            .size(56.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .semantics {
                role = androidx.compose.ui.semantics.Role.Button
                contentDescription?.let { this.contentDescription = it }
            },
        style = GlassStyle.Thick,
        shape = CircleShape,
        interaction = GlassInteraction.Pressable,
        interactionSource = interactionSource,
        elevation = GlassTokens.shadowElevationStrong,
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { content() }
    }
}
