package com.example.wallet.core.design.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

/**
 * A single transaction line (plan.md §7/§23), rendered as its own small glass card so every list
 * of transactions reads as a stack of distinct glass tiles rather than a flat list. Deliberately
 * takes primitives, not a domain `Transaction`/`TransactionType` — keeps the design system
 * decoupled from the domain layer, consistent with every other component here.
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
    isTransfer: Boolean = false,
) {
    val amountColor = when {
        isTransfer -> MaterialTheme.colorScheme.onSurfaceVariant
        isIncome -> WalletTheme.extendedColors.income
        else -> WalletTheme.extendedColors.expense
    }
    val sign = if (isIncome) "+" else "-"

    GlassSurface(
        modifier = Modifier.fillMaxWidth(),
        style = GlassStyle.Thick,
        shape = GlassShapes.small,
        // Same neutral-fill-blended-with-accent treatment as CashFlowCard — reads as tinted glass
        // instead of GlassStyle.Thin's low-alpha fill, which looked like a flat black bar.
        fill = GlassColors.neutralTintedFill(WalletTheme.extendedColors.transfer),
        // No drop shadow: at this small size/elevation, Android's shadow renders as a tight grey
        // ring hugging the card edge rather than a soft lift — on a white/light background that
        // reads as an unwanted outline. The fill-color contrast alone defines the card edge.
        elevation = 0.dp,
        // wallet_app_stability_performance_plan.md §17-20 — this row lives in the transaction
        // list's LazyColumn, the app's single most performance-sensitive scroll surface.
        lightweight = true,
    ) {
        ListItem(
            modifier = Modifier.clickable(onClick = onClick),
            headlineContent = { Text(title, fontSize = 15.sp) },
            supportingContent = { Text(subtitle, fontSize = 12.sp) },
            trailingContent = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$sign ${formatMoney(amountMinor, currency)}",
                        color = amountColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
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
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        )
    }
}
