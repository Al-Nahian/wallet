package com.example.wallet.core.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** The standard four-square Microsoft mark, for the "Continue with Microsoft" sign-in row —
 * required by Microsoft's sign-in branding guidelines, not an arbitrary generic icon. */
@Composable
fun MicrosoftLogoIcon(modifier: Modifier = Modifier, size: Dp = 20.dp) {
    val gap = 2.dp
    val tile = (size - gap) / 2
    Column(modifier = modifier.size(size), verticalArrangement = Arrangement.spacedBy(gap)) {
        Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
            Box(modifier = Modifier.size(tile).background(Color(0xFFF25022)))
            Box(modifier = Modifier.size(tile).background(Color(0xFF7FBA00)))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
            Box(modifier = Modifier.size(tile).background(Color(0xFF00A4EF)))
            Box(modifier = Modifier.size(tile).background(Color(0xFFFFB900)))
        }
    }
}
