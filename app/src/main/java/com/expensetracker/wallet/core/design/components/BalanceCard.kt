package com.expensetracker.wallet.core.design.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.expensetracker.wallet.core.common.formatMoney
import com.expensetracker.wallet.core.design.glass.liquidGlassContentColor

/** A full-width, frosted glass balance card (Total Balance, Savings, Income, Expense…) —
 * [containerColor] doubles as the glass tint and [icon]'s bubble color. See [LiquidGlassCard] for
 * how this differs between dark mode (flat vivid-tint-with-white-text) and light mode (real
 * liquid-glass refraction over a pastel fill, matching the reference design). */
@Composable
fun BalanceCard(
    label: String,
    amountMinor: Long,
    currency: String,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    contentColor: Color = liquidGlassContentColor(containerColor),
    icon: ImageVector = Icons.Filled.AccountBalanceWallet,
    waveVariant: Int? = null,
    onClick: (() -> Unit)? = null,
) {
    LiquidGlassCard(
        modifier = modifier.fillMaxWidth(),
        tint = containerColor,
        waveVariant = waveVariant ?: defaultWaveVariant(containerColor),
    ) {
        Row(
            modifier = Modifier
                .let { if (onClick != null) it.clickable(onClick = onClick) else it }
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LiquidIconBubble(icon = icon, tint = containerColor, size = 36.dp)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    fontSize = 13.sp,
                    color = contentColor.copy(alpha = 0.95f),
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = formatMoney(amountMinor, currency),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = contentColor,
                )
            }
            // Only implies a drill-down when there's actually somewhere to drill into — a bare
            // decorative chevron with no [onClick] misleads users into tapping a dead card.
            if (onClick != null) {
                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = null,
                    tint = contentColor.copy(alpha = 0.75f),
                )
            }
        }
    }
}
