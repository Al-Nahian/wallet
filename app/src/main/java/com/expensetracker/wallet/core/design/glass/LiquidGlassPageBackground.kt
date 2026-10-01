package com.expensetracker.wallet.core.design.glass

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.expensetracker.wallet.R

/** The reference design's dark-mode page background — a real image (soft indigo/teal/violet glow
 * blobs on a deep navy base) rather than a flat black fill, so top bar, cards and the blurred
 * strip behind the bottom nav all sit on top of it. Cropped to fill and top-aligned so the
 * brightest blobs (top corners) land behind the top bar/dashboard header, matching the reference.
 *
 * Blurred further and darkened with a black scrim on top of the image's own baked-in softness —
 * the raw image read as too sharp/bright behind cards and text once seen at full brightness on
 * a real screen. [Modifier.blur] is a no-op below API 31, same as every other real-time effect
 * this app gates on that version; the darkening scrim still applies regardless. */
@Composable
fun LiquidDarkPageBackground(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.liquid_glass_dark_bg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize().blur(48.dp),
        )
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.55f)))
    }
}
