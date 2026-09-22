package com.example.wallet.core.design.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.wallet.core.common.formatMoney

/** [containerColor]/[contentColor] default to a neutral surface (existing call sites keep their
 * current look); the dashboard tints these per-card (plan.md §20 — total balance, income,
 * expense, savings each read at a glance via color, not just label text). */
@Composable
fun BalanceCard(
    label: String,
    amountMinor: Long,
    currency: String,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor, contentColor = contentColor),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = contentColor.copy(alpha = 0.85f),
            )
            Text(
                text = formatMoney(amountMinor, currency),
                style = MaterialTheme.typography.titleLarge,
                color = contentColor,
            )
        }
    }
}
