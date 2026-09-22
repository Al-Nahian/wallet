package com.example.wallet.core.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.wallet.core.design.parseHexColor

/** A colored circle with the category/group's initial letter — [color] always comes from the
 * DB (plan.md §69 rule 8), never a hardcoded per-name lookup, since categories don't have real
 * icon assets seeded yet. */
@Composable
fun CategoryIcon(name: String, color: String, modifier: Modifier = Modifier, size: Dp = 40.dp) {
    val backgroundColor = remember(color) { parseHexColor(color) }
    Box(
        modifier = modifier.size(size).clip(CircleShape).background(backgroundColor),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = name.trim().take(1).uppercase(),
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
        )
    }
}
