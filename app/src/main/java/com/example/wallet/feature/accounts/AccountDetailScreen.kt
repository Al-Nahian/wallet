package com.example.wallet.feature.accounts

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.wallet.core.design.components.BalanceCard
import com.example.wallet.core.design.components.ConfirmationDialog
import com.example.wallet.core.design.components.EmptyState
import com.example.wallet.core.design.components.GlassScreenTopBar
import com.example.wallet.core.design.components.SecondaryButton
import com.example.wallet.core.design.components.TransactionRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountDetailScreen(
    onBack: () -> Unit,
    onEdit: (String) -> Unit,
    onArchived: () -> Unit,
    onTransactionClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AccountDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showArchiveConfirmation by remember { mutableStateOf(false) }
    var pendingDeleteTransactionId by remember { mutableStateOf<String?>(null) }

    Scaffold(
        modifier = modifier,
        topBar = {
            GlassScreenTopBar(
                title = uiState.account?.name ?: "Account",
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
                        .fillMaxSize(),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
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
                    }

                    if (uiState.transactions.isEmpty()) {
                        EmptyState(
                            title = "No transactions yet",
                            subtitle = "Transactions you record against this account show up here.",
                            icon = Icons.Filled.Receipt,
                            modifier = Modifier.weight(1f),
                        )
                    } else {
                        LazyColumn(modifier = Modifier.weight(1f)) {
                            items(uiState.transactions, key = { it.id }) { transaction ->
                                TransactionRow(
                                    title = transaction.title,
                                    subtitle = transaction.subtitle,
                                    amountMinor = transaction.amountMinor,
                                    currency = transaction.currency,
                                    isIncome = transaction.isIncome,
                                    isTransfer = transaction.isTransfer,
                                    onClick = {
                                        if (transaction.isTransfer) {
                                            Toast.makeText(context, "Transfers can't be edited yet — delete and re-create if needed.", Toast.LENGTH_SHORT).show()
                                        } else {
                                            onTransactionClick(transaction.id)
                                        }
                                    },
                                    onDelete = { pendingDeleteTransactionId = transaction.id },
                                )
                            }
                        }
                    }
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

    val deleteId = pendingDeleteTransactionId
    if (deleteId != null) {
        ConfirmationDialog(
            title = "Delete this transaction?",
            message = "This can't be undone from here, but the record stays recoverable in the database.",
            confirmLabel = "Delete",
            onConfirm = {
                viewModel.deleteTransaction(deleteId)
                pendingDeleteTransactionId = null
            },
            onDismiss = { pendingDeleteTransactionId = null },
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
