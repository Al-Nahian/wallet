package com.example.wallet.core.design.components

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
import com.example.wallet.core.common.formatMoney
import com.example.wallet.core.design.glass.GlassStyle
import com.example.wallet.core.design.glass.GlassSurface

/** A full-width, vividly tinted glass balance card (Total Balance, Savings, Income, Expense…) —
 * [containerColor] doubles as the glass tint/glow color and [icon]'s bubble color.
 *
 * [frosted] swaps the usual ~0.90-alpha Vivid fill for a lighter 0.55 one — the same tint the
 * nav bar uses over its backdrop blur, minus the blur itself: this card sits in the normal
 * scroll flow with nothing passing behind it, so there's nothing to blur, just a softer, more
 * translucent version of the same color. It also drops the glow shadow: same reasoning as the
 * bar's own shadow removal — a colored shadow cast under a fill this translucent shows straight
 * through as a light band along the bottom edge instead of a lift. */
@Composable
fun BalanceCard(
    label: String,
    amountMinor: Long,
    currency: String,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    icon: ImageVector = Icons.Filled.AccountBalanceWallet,
    frosted: Boolean = false,
) {
    GlassSurface(
        modifier = modifier.fillMaxWidth(),
        style = GlassStyle.Vivid,
        tint = containerColor,
        fill = if (frosted) containerColor.copy(alpha = 0.55f) else null,
        glow = !frosted,
        elevation = if (frosted) 0.dp else 10.dp,
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            GlassIconBubble(icon = icon, tint = containerColor, size = 36.dp)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    fontSize = 13.sp,
                    color = contentColor.copy(alpha = 0.85f),
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = formatMoney(amountMinor, currency),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = contentColor,
                )
            }
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = contentColor.copy(alpha = 0.6f),
            )
        }
    }
}
