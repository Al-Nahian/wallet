package com.example.wallet.core.design.glass

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.wallet.R

/** The reference design's dark-mode page background — a real image (soft indigo/teal/violet glow
 * blobs on a deep navy base) rather than a flat black fill, so top bar, cards and the blurred
 * strip behind the bottom nav all sit on top of it. Cropped to fill and top-aligned so the
 * brightest blobs (top corners) land behind the top bar/dashboard header, matching the reference. */
@Composable
fun LiquidDarkPageBackground(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(R.drawable.liquid_glass_dark_bg),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier.fillMaxSize(),
    )
}
