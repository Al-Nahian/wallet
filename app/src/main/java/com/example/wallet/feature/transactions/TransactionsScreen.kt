package com.example.wallet.feature.transactions

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.wallet.core.design.components.EmptyState

@Composable
fun TransactionsScreen(modifier: Modifier = Modifier) {
    EmptyState(
        title = "No transactions yet",
        subtitle = "Adding expenses and income arrives in Phase 4.",
        icon = Icons.Filled.Receipt,
        modifier = modifier,
    )
}
