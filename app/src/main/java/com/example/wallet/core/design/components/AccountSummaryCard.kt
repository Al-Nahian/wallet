package com.example.wallet.core.design.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wallet.core.common.formatMoney
import com.example.wallet.core.design.glass.liquidGlassContentColor

/** A compact, frosted tinted glass account tile for the dashboard's two-per-row accounts grid —
 * a glass icon bubble and trailing chevron over a lighter, translucent tint, per the
 * liquid-glass reference design. See [LiquidGlassCard]/[liquidGlassContentColor] for how this
 * differs between light mode (pastel fill, dark text, real refraction) and dark mode (vivid fill,
 * white text). */
@Composable
fun AccountSummaryCard(
    label: String,
    amountMinor: Long,
    currency: String,
    icon: ImageVector,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    waveVariant: Int? = null,
    onClick: (() -> Unit)? = null,
) {
    val contentColor = liquidGlassContentColor(backgroundColor)
    LiquidGlassCard(
        modifier = modifier.fillMaxWidth(),
        tint = backgroundColor,
        waveVariant = waveVariant ?: defaultWaveVariant(backgroundColor),
        lightweight = true,
    ) {
        Row(
            modifier = Modifier
                .let { if (onClick != null) it.clickable(onClick = onClick) else it }
                .padding(horizontal = 12.dp, vertical = 14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LiquidIconBubble(icon = icon, tint = backgroundColor, size = 28.dp)
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    fontSize = 11.sp,
                    color = contentColor.copy(alpha = 0.95f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text = formatMoney(amountMinor, currency),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = contentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (onClick != null) {
                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = null,
                    tint = contentColor.copy(alpha = 0.75f),
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}
