package com.example.wallet.core.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.wallet.core.common.formatMoney
import com.example.wallet.core.design.WalletTheme

/** A proportional two-color bar comparing this period's income to its expenses (plan.md §20 —
 * "How much did I spend/earn?" at a glance), plus a small color-keyed legend. Renders a neutral
 * empty bar when both are zero rather than dividing by zero. */
@Composable
fun IncomeExpenseBar(incomeMinor: Long, expenseMinor: Long, currency: String, modifier: Modifier = Modifier) {
    val total = incomeMinor + expenseMinor
    val incomeFraction = if (total <= 0L) 0.5f else (incomeMinor.toFloat() / total).coerceIn(0f, 1f)

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Box(
                modifier = Modifier
                    .weight(incomeFraction.coerceAtLeast(0.001f))
                    .fillMaxWidth()
                    .background(WalletTheme.extendedColors.income),
            )
            Box(
                modifier = Modifier
                    .weight((1f - incomeFraction).coerceAtLeast(0.001f))
                    .fillMaxWidth()
                    .background(WalletTheme.extendedColors.expense),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            LegendDot(color = WalletTheme.extendedColors.income, label = "Income ${formatMoney(incomeMinor, currency)}")
            LegendDot(color = WalletTheme.extendedColors.expense, label = "Expense ${formatMoney(expenseMinor, currency)}")
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
        Spacer(Modifier.size(6.dp))
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
