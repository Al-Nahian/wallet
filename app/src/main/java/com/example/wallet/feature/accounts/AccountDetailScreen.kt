package com.example.wallet.feature.accounts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.wallet.core.design.components.BalanceCard
import com.example.wallet.core.design.components.ConfirmationDialog
import com.example.wallet.core.design.components.EmptyState
import com.example.wallet.core.design.components.SecondaryButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountDetailScreen(
    onBack: () -> Unit,
    onEdit: (String) -> Unit,
    onArchived: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AccountDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showArchiveConfirmation by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(uiState.account?.name ?: "Account") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { paddingValues ->
        val account = uiState.account

        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            account == null -> {
                EmptyState(
                    title = uiState.errorMessage ?: "Account not found",
                    modifier = Modifier.padding(paddingValues),
                )
            }

            else -> {
                Column(
                    modifier = Modifier
                        .padding(paddingValues)
                        .padding(16.dp)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    BalanceCard(
                        label = "Balance",
                        amountMinor = account.balanceMinor,
                        currency = account.currency,
                    )

                    Text(text = account.type.label(), style = MaterialTheme.typography.bodyMedium)
                    if (account.institutionName != null) {
                        Text(text = account.institutionName, style = MaterialTheme.typography.bodyMedium)
                    }

                    ActionButtonsRow(
                        onEdit = { onEdit(account.id) },
                        onArchive = { showArchiveConfirmation = true },
                    )

                    EmptyState(
                        title = "No transactions yet",
                        subtitle = "Adding expenses and income arrives in Phase 5.",
                        icon = Icons.Filled.Receipt,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
    }

    if (showArchiveConfirmation) {
        ConfirmationDialog(
            title = "Archive this account?",
            message = "It'll be hidden from your accounts list, but nothing is deleted.",
            confirmLabel = "Archive",
            onConfirm = {
                showArchiveConfirmation = false
                viewModel.archive(onDone = onArchived)
            },
            onDismiss = { showArchiveConfirmation = false },
        )
    }
}

@Composable
private fun ActionButtonsRow(onEdit: () -> Unit, onArchive: () -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        SecondaryButton(text = "Edit", onClick = onEdit, modifier = Modifier.weight(1f))
        SecondaryButton(text = "Archive", onClick = onArchive, modifier = Modifier.weight(1f))
    }
}
