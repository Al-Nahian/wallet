package com.example.wallet.core.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wallet.core.common.formatMoney
import com.example.wallet.core.design.glass.GlassStyle
import com.example.wallet.core.design.glass.GlassSurface
import com.example.wallet.core.design.glass.GlassTokens
import java.util.Locale

/** plan.md §7/§17 — a single budget's spent/remaining/usage at a glance, in the format §17
 * specifies ("Budget: X / Spent: Y / Remaining: Z / Usage: N%"), colored by status (healthy,
 * near limit, exceeded) as a frosted tinted glass card matching the dashboard's other headline
 * cards (see [BalanceCard] for the frosted-fill/no-shadow reasoning). */
@Composable
fun BudgetProgress(
    name: String,
    amountMinor: Long,
    spentMinor: Long,
    remainingMinor: Long,
    usagePercent: Double,
    currency: String,
    modifier: Modifier = Modifier,
) {
    val statusColor = when {
        usagePercent >= 100.0 -> Color(0xFFDC2626)
        usagePercent >= 80.0 -> Color(0xFFEA580C)
        else -> Color(0xFF16A34A)
    }

    GlassSurface(
        modifier = modifier.fillMaxWidth(),
        style = GlassStyle.Vivid,
        tint = statusColor,
        fill = statusColor.copy(alpha = GlassTokens.frostedFillAlpha),
        glow = false,
        elevation = 0.dp,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                )
                Text(
                    text = "${String.format(Locale.getDefault(), "%.1f", usagePercent)}%",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                )
            }
            Spacer(Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.White.copy(alpha = 0.25f)),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth((usagePercent / 100.0).toFloat().coerceIn(0f, 1f))
                        .fillMaxSize()
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.White),
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = "Budget: ${formatMoney(amountMinor, currency)}  ·  Spent: ${formatMoney(spentMinor, currency)}  ·  " +
                    "Remaining: ${formatMoney(remainingMinor, currency)}",
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.95f),
            )
        }
    }
}
