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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.wallet.core.common.formatMoney
import java.util.Locale
import kotlin.math.abs

private val CashFlowIncomeColor = Color(0xFF16A34A)
private val CashFlowExpenseColor = Color(0xFFDC2626)

private data class CashFlowPalette(val background: Color, val text: Color, val mutedText: Color, val track: Color)

private val DarkCashFlowPalette = CashFlowPalette(
    background = Color(0xFF1C1C1E),
    text = Color.White,
    mutedText = Color(0xFFAEAEB2),
    track = Color(0xFF3A3A3C),
)
private val LightCashFlowPalette = CashFlowPalette(
    background = Color(0xFFF2F2F7),
    text = Color(0xFF1C1C1E),
    mutedText = Color(0xFF6D6D72),
    track = Color(0xFFE0E0E5),
)

/** plan.md §20 — income vs. expense at a glance, styled as an elevated card with a fixed
 * light/dark palette pair (so it reads correctly whichever theme is active, unlike a single
 * hardcoded dark background) rather than the theme-tinted cards elsewhere on the dashboard.
 * [previousNetMinor], when given, renders a "vs past period" delta; omit it to hide that row. */
@Composable
fun CashFlowCard(
    periodLabel: String,
    incomeMinor: Long,
    expenseMinor: Long,
    currency: String,
    modifier: Modifier = Modifier,
    previousNetMinor: Long? = null,
) {
    val palette = if (isSystemInDarkTheme()) DarkCashFlowPalette else LightCashFlowPalette
    val netMinor = incomeMinor - expenseMinor
    val changePercent = previousNetMinor
        ?.takeIf { it != 0L }
        ?.let { previous -> (netMinor - previous) * 100.0 / abs(previous) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = palette.background, contentColor = palette.text),
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "Cash Flow",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = palette.text,
            )
            Spacer(Modifier.height(14.dp))

            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = periodLabel.uppercase(Locale.getDefault()),
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.mutedText,
                )
                if (changePercent != null) {
                    Text(text = "vs past period", style = MaterialTheme.typography.labelSmall, color = palette.mutedText)
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = formatMoney(netMinor, currency),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = palette.text,
                )
                if (changePercent != null) {
                    val sign = if (changePercent >= 0) "+" else ""
                    Text(
                        text = "$sign${String.format(Locale.getDefault(), "%.0f", changePercent)}%",
                        style = MaterialTheme.typography.titleMedium,
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
            Text(text = label, style = MaterialTheme.typography.bodyMedium, color = textColor)
            Text(text = formatMoney(amountMinor, currency), style = MaterialTheme.typography.bodyMedium, color = textColor)
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
