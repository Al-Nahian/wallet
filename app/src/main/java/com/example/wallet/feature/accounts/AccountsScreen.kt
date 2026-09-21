package com.example.wallet.feature.accounts

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.wallet.core.design.components.EmptyState

@Composable
fun AccountsScreen(modifier: Modifier = Modifier) {
    EmptyState(
        title = "No accounts yet",
        subtitle = "Creating accounts arrives in Phase 3.",
        icon = Icons.Filled.AccountBalanceWallet,
        modifier = modifier,
    )
}
