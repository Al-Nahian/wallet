package com.example.wallet.core.design.glass

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

/** Deep-tinted text for a [com.example.wallet.core.design.components.LiquidGlassCard] — light
 * mode's pastel fill needs dark, tint-saturated text; dark mode's deepened fill still wants
 * white/light text, same as before. */
@Composable
fun liquidGlassContentColor(tint: Color): Color =
    if (isSystemInDarkTheme()) Color.White else lerp(tint, Color.Black, 0.55f)
