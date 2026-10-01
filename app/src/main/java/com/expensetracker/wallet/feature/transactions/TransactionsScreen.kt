package com.expensetracker.wallet.feature.transactions

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.expensetracker.wallet.core.design.components.ConfirmationDialog
import com.expensetracker.wallet.core.design.components.EmptyState
import com.expensetracker.wallet.core.design.components.TransactionGroupHeader
import com.expensetracker.wallet.core.design.components.TransactionRow
import com.expensetracker.wallet.core.design.components.WalletBottomNavSpace

@Composable
fun TransactionsScreen(
    onAddTransaction: () -> Unit,
    onTransactionClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TransactionsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var pendingDeleteId by remember { mutableStateOf<String?>(null) }

    when (val state = uiState) {
        TransactionsUiState.Loading -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        is TransactionsUiState.Loaded -> {
            if (state.groups.isEmpty()) {
                EmptyState(
                    title = "No transactions yet",
                    subtitle = "Tap + to record your first expense or income.",
                    icon = Icons.Filled.Receipt,
                    actionLabel = "Add transaction",
                    onAction = onAddTransaction,
                    modifier = modifier,
                )
            } else {
                Box(modifier = modifier.fillMaxSize()) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 8.dp,
                            bottom = 8.dp + WalletBottomNavSpace,
                        ),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        item {
                            // Structural placeholder per plan.md §23 — real search/filter logic
                            // slots in here once Phase 6/7 add the fields it needs to filter on.
                            SearchFilterBar(
                                onSearchClick = {
                                    Toast.makeText(context, "Search arrives in a later phase.", Toast.LENGTH_SHORT).show()
                                },
                                onFilterClick = {
                                    Toast.makeText(context, "Filters arrive in a later phase.", Toast.LENGTH_SHORT).show()
                                },
                            )
                        }
                        state.groups.forEach { group ->
                            item(key = "header-${group.dateLabel}") {
                                TransactionGroupHeader(group.dateLabel)
                            }
                            items(group.transactions, key = { it.id }) { transaction ->
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
                                    onDelete = { pendingDeleteId = transaction.id },
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    val deleteId = pendingDeleteId
    if (deleteId != null) {
        ConfirmationDialog(
            title = "Delete this transaction?",
            message = "This can't be undone from here, but the record stays recoverable in the database.",
            confirmLabel = "Delete",
            onConfirm = {
                viewModel.deleteTransaction(deleteId)
                pendingDeleteId = null
            },
            onDismiss = { pendingDeleteId = null },
        )
    }
}

@Composable
private fun SearchFilterBar(onSearchClick: () -> Unit, onFilterClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.End,
    ) {
        IconButton(onClick = onSearchClick) {
            Icon(Icons.Filled.Search, contentDescription = "Search transactions")
        }
        IconButton(onClick = onFilterClick) {
            Icon(Icons.Filled.FilterList, contentDescription = "Filter transactions")
        }
    }
}
