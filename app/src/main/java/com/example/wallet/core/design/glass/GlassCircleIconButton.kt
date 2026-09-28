package com.example.wallet.core.design.glass

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.wallet.core.design.WalletTheme

/**
 * A small circular glass bubble around a single icon — the top bar's profile/notification
 * buttons (and any other standalone chrome icon) per the liquid-glass reference design, where the
 * bar itself carries no visible plate and only the individual controls read as glass.
 */
@Composable
fun GlassCircleIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    content: @Composable () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    // Dark-mode fallback matches the reference's steel-blue bead; light mode keeps the
    // neutral-tinted fill (see LiquidCircleIconButton's dark branch for the live path).
    val fallbackFill = if (isSystemInDarkTheme()) {
        Color(0xFF5B7A9C).copy(alpha = 0.75f)
    } else {
        GlassColors.neutralTintedFill(WalletTheme.extendedColors.transfer)
    }
    GlassSurface(
        modifier = modifier
            .size(40.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .semantics {
                role = Role.Button
                contentDescription?.let { this.contentDescription = it }
            },
        // Same neutral-fill-blended-with-accent treatment as every other "black card" in the app,
        // rather than a drop shadow — on this fully circular shape, the shadow rendered as a
        // visible darker crescent along the bottom edge instead of a soft lift.
        style = GlassStyle.Thick,
        shape = CircleShape,
        fill = fallbackFill,
        interaction = GlassInteraction.Pressable,
        interactionSource = interactionSource,
        elevation = 0.dp,
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { content() }
    }
}
