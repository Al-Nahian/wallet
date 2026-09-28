package com.example.wallet.core.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wallet.core.common.formatMoney
import com.example.wallet.core.design.WalletTheme

/**
 * A single transaction line (plan.md §7/§23), rendered as its own small glass card so every list
 * of transactions reads as a stack of distinct glass tiles rather than a flat list. Deliberately
 * takes primitives, not a domain `Transaction`/`TransactionType` — keeps the design system
 * decoupled from the domain layer, consistent with every other component here.
 *
 * Tinted by entry type (recent-transaction-reference.png): expense cards glow red, income cards
 * green, transfer cards blue — the card fill, leading bubble glyph and amount all follow the
 * same tint. The leading bubble always shows a directional type glyph (↑ income, ↓ expense,
 * ⇄ transfer), never letters. In light mode the bubble is a light glass bead (translucent white
 * gradient, light border, soft tinted glow); in dark mode it stays a dark translucent bead.
 * The trailing delete control is a dark translucent bubble like the reference's.
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
    val cardTint = when {
        isTransfer -> WalletTheme.extendedColors.transfer
        isIncome -> WalletTheme.extendedColors.income
        else -> WalletTheme.extendedColors.expense
    }
    val amountColor = cardTint
    val sign = if (isIncome) "+" else "-"
    // Lightened toward white for glyphs on the dark bubble — the reference's icons read as pale
    // pink/green/blue rather than the full-strength tint.
    val glyphTint = lerp(cardTint, Color.White, 0.4f)
    val typeIcon = when {
        isTransfer -> Icons.Filled.SwapHoriz
        isIncome -> Icons.Filled.ArrowUpward
        else -> Icons.Filled.ArrowDownward
    }
    val isDark = isSystemInDarkTheme()

    LiquidGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Button,
                onClick = onClick,
            ),
        tint = cardTint,
        // wallet_app_stability_performance_plan.md §17-20 — this row lives in scrolling
        // LazyColumns, the app's most performance-sensitive scroll surfaces. Skips the drop
        // shadow (see LiquidGlassCard's own doc on `lightweight`).
        lightweight = true,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (isDark) {
                Box(
                    modifier = Modifier.size(44.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(imageVector = typeIcon, contentDescription = null, tint = glyphTint, modifier = Modifier.size(22.dp))
                }
            } else {
                // Light-mode glass bead: translucent white gradient, light border and a soft
                // tinted glow — the dark bead from dark mode read as dull and flat here.
                // Full-strength tint glyph stays legible on the near-white fill.
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .shadow(
                            elevation = 6.dp,
                            shape = CircleShape,
                            clip = false,
                            ambientColor = cardTint.copy(alpha = 0.35f),
                            spotColor = cardTint.copy(alpha = 0.35f),
                        )
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    Color.White.copy(alpha = 0.8f),
                                    Color.White.copy(alpha = 0.35f),
                                ),
                            ),
                        )
                        .border(width = 1.dp, color = Color.White.copy(alpha = 0.7f), shape = CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(imageVector = typeIcon, contentDescription = null, tint = cardTint, modifier = Modifier.size(22.dp))
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.width(8.dp))
            Text(
                text = "$sign ${formatMoney(amountMinor, currency)}",
                color = amountColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
            )
            Spacer(Modifier.width(4.dp))
            Box(
                modifier = Modifier.size(36.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center,
            ) {
                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = "Delete transaction",
                        tint = Color.White.copy(alpha = 0.75f),
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }
    }
}
