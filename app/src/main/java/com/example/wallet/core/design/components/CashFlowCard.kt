package com.example.wallet.core.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wallet.core.common.formatMoney
import com.example.wallet.core.design.WalletTheme
import com.example.wallet.core.design.glass.GlassColors
import com.example.wallet.core.design.glass.GlassShapes
import com.example.wallet.core.design.glass.GlassStyle
import com.example.wallet.core.design.glass.GlassSurface
import java.util.Locale
import kotlin.math.abs

private val CashFlowIncomeColor = Color(0xFF22C55E)
private val CashFlowExpenseColor = Color(0xFFEF4444)
private val CashFlowIconTint = Color(0xFF64748B)

private data class CashFlowPalette(val text: Color, val mutedText: Color, val track: Color)

private val DarkCashFlowPalette = CashFlowPalette(
    text = Color.White,
    mutedText = Color(0xFFAEAEB2),
    track = Color(0xFF3A3A3C),
)
private val LightCashFlowPalette = CashFlowPalette(
    text = Color(0xFF1C1C1E),
    mutedText = Color(0xFF6D6D72),
    track = Color(0xFFE0E0E5),
)

/** plan.md §20 — income vs. expense at a glance, styled as a neutral dark glass card (unlike the
 * vividly tinted account/balance/stat cards) with an icon bubble and a "This Month ⌄" period
 * label, per the liquid-glass reference design. [previousNetMinor], when given, renders a "vs
 * past period" delta; omit it to hide that row. */
@Composable
fun CashFlowCard(
    periodLabel: String,
    incomeMinor: Long,
    expenseMinor: Long,
    currency: String,
    modifier: Modifier = Modifier,
    previousNetMinor: Long? = null,
) {
    val isDark = isSystemInDarkTheme()
    val palette = if (isDark) DarkCashFlowPalette else LightCashFlowPalette
    val netMinor = incomeMinor - expenseMinor
    val changePercent = previousNetMinor
        ?.takeIf { it != 0L }
        ?.let { previous -> (netMinor - previous) * 100.0 / abs(previous) }

    GlassSurface(
        modifier = modifier.fillMaxWidth(),
        style = GlassStyle.Thick,
        shape = GlassShapes.large,
        fill = GlassColors.neutralTintedFill(WalletTheme.extendedColors.transfer),
        elevation = 0.dp,
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    GlassIconBubble(icon = Icons.Filled.SwapHoriz, tint = CashFlowIconTint, size = 32.dp)
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "Cash Flow",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = palette.text,
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = periodLabel, style = MaterialTheme.typography.labelLarge, color = palette.mutedText)
                    Icon(
                        imageVector = Icons.Filled.ExpandMore,
                        contentDescription = null,
                        tint = palette.mutedText,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
            Spacer(Modifier.height(14.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = formatMoney(netMinor, currency),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = palette.text,
                )
                if (changePercent != null) {
                    val sign = if (changePercent >= 0) "+" else ""
                    Text(
                        text = "$sign${String.format(Locale.getDefault(), "%.0f", changePercent)}%",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (changePercent >= 0) CashFlowIncomeColor else CashFlowExpenseColor,
                    )
                }
            }

            Spacer(Modifier.height(18.dp))
            val maxMagnitude = maxOf(incomeMinor, expenseMinor, 1L)
            CashFlowRow(
                label = "Income",
                amountMinor = incomeMinor,
                currency = currency,
                textColor = palette.text,
                trackColor = palette.track,
                barColor = CashFlowIncomeColor,
                fraction = incomeMinor.toFloat() / maxMagnitude,
            )
            Spacer(Modifier.height(14.dp))
            CashFlowRow(
                label = "Expenses",
                amountMinor = -expenseMinor,
                currency = currency,
                textColor = palette.text,
                trackColor = palette.track,
                barColor = CashFlowExpenseColor,
                fraction = expenseMinor.toFloat() / maxMagnitude,
            )
        }
    }
}

@Composable
private fun CashFlowRow(
    label: String,
    amountMinor: Long,
    currency: String,
    textColor: Color,
    trackColor: Color,
    barColor: Color,
    fraction: Float,
) {
    Column {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text(text = label, fontSize = 13.sp, color = textColor)
            Text(text = formatMoney(amountMinor, currency), fontSize = 13.sp, color = textColor)
        }
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(trackColor),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction.coerceIn(0f, 1f))
                    .fillMaxSize()
                    .clip(RoundedCornerShape(4.dp))
                    .background(barColor),
            )
        }
    }
}
