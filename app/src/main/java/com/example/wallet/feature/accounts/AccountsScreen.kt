package com.example.wallet.feature.accounts

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.wallet.core.common.formatMoney
import com.example.wallet.core.design.components.EmptyState

@Composable
fun AccountsScreen(
    onAccountClick: (String) -> Unit,
    onAddAccount: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AccountsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val state = uiState) {
        AccountsUiState.Loading -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        is AccountsUiState.Loaded -> {
            if (state.accounts.isEmpty()) {
                EmptyState(
                    title = "No accounts yet",
                    subtitle = "Add a bank, cash, card or mobile wallet account to start tracking.",
                    icon = Icons.Filled.AccountBalanceWallet,
                    actionLabel = "Add account",
                    onAction = onAddAccount,
                    modifier = modifier,
                )
            } else {
                LazyColumn(modifier = modifier.fillMaxSize()) {
                    items(state.accounts, key = { it.id }) { account ->
                        AccountRow(account = account, onClick = { onAccountClick(account.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun AccountRow(account: AccountUi, onClick: () -> Unit) {
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        leadingContent = { Icon(imageVector = account.type.icon(), contentDescription = null) },
        headlineContent = { Text(account.name) },
        supportingContent = { Text(account.type.label()) },
        trailingContent = {
            Text(
                text = formatMoney(account.balanceMinor, account.currency),
                style = MaterialTheme.typography.titleMedium,
            )
        },
    )
}
