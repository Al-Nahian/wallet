package com.example.wallet.core.design.glass

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Shape

/** liquid_glass_implementation_plan.md §6/§12 — restrained, consistently rounded geometry. */
object GlassShapes {
    val small: Shape = RoundedCornerShape(GlassTokens.cornerSmall)
    val medium: Shape = RoundedCornerShape(GlassTokens.cornerMedium)
    val large: Shape = RoundedCornerShape(GlassTokens.cornerLarge)

    /** Rounds only the top corners — for a bar flush against the *bottom* screen edge (e.g. the
     * bottom nav), where its bottom edge meets the screen and only the top edge reads as a
     * material boundary. */
    fun topRounded(radius: androidx.compose.ui.unit.Dp = GlassTokens.cornerLarge): Shape =
        RoundedCornerShape(topStart = radius, topEnd = radius)

    /** Rounds only the bottom corners — for a bar flush against the *top* screen edge. */
    fun bottomRounded(radius: androidx.compose.ui.unit.Dp = GlassTokens.cornerLarge): Shape =
        RoundedCornerShape(bottomStart = radius, bottomEnd = radius)
}
