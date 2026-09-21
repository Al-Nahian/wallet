package com.example.wallet.core.design.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.wallet.core.common.formatMoney
import com.example.wallet.core.design.WalletTheme

/**
 * A single transaction line (plan.md §7/§23). Deliberately takes primitives, not a domain
 * `Transaction`/`TransactionType` — keeps the design system decoupled from the domain layer,
 * consistent with every other component here.
 */
@Composable
fun TransactionRow(
    title: String,
    subtitle: String,
    amountMinor: Long,
    currency: String,
    isIncome: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    val amountColor = if (isIncome) WalletTheme.extendedColors.income else WalletTheme.extendedColors.expense
    val sign = if (isIncome) "+" else "-"

    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "$sign ${formatMoney(amountMinor, currency)}",
                    color = amountColor,
                    style = MaterialTheme.typography.titleMedium,
                )
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = "Delete transaction",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        },
    )
}
