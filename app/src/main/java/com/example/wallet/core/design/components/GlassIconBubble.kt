package com.example.wallet.core.design.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.wallet.core.design.glass.GlassStyle
import com.example.wallet.core.design.glass.GlassSurface

/** A small circular glass badge for a card's leading icon — every headline dashboard/report card
 * (account tile, balance card, stat card, cash flow card) frames its icon this way instead of a
 * bare [Icon], matching the liquid-glass reference's "glossy bubble" icon treatment. */
@Composable
fun GlassIconBubble(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    tint: Color? = null,
    size: Dp = 32.dp,
    iconTint: Color = Color.White,
) {
    GlassSurface(
        modifier = modifier.size(size),
        style = GlassStyle.Thick,
        shape = CircleShape,
        tint = tint,
        elevation = 2.dp,
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(size * 0.5f))
        }
    }
}
