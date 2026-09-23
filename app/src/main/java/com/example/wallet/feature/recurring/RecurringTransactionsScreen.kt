package com.example.wallet.feature.recurring

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.wallet.core.common.dateGroupLabel
import com.example.wallet.core.common.formatMoney
import com.example.wallet.core.design.WalletTheme
import com.example.wallet.core.design.components.ConfirmationDialog
import com.example.wallet.core.design.components.EmptyState
import com.example.wallet.core.design.components.GlassScreenTopBar
import com.example.wallet.core.design.glass.GlassShapes
import com.example.wallet.core.design.glass.GlassStyle
import com.example.wallet.core.design.glass.GlassSurface
import com.example.wallet.domain.model.TransactionType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurringTransactionsScreen(
    onBack: () -> Unit,
    onAdd: () -> Unit,
    onEdit: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RecurringTransactionsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var pendingDeleteId by remember { mutableStateOf<String?>(null) }

    Scaffold(
        modifier = modifier,
        topBar = {
            GlassScreenTopBar(
                title = "Recurring Payments",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onAdd) {
                        Icon(Icons.Filled.Add, contentDescription = "New recurring payment")
                    }
                },
            )
        },
    ) { paddingValues ->
        when (val state = uiState) {
            RecurringTransactionsUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            is RecurringTransactionsUiState.Loaded -> {
                if (state.items.isEmpty()) {
                    EmptyState(
                        title = "No recurring payments yet",
                        subtitle = "Add a bill, subscription or regular income so it doesn't need to be entered by hand every time.",
                        icon = Icons.Filled.Autorenew,
                        actionLabel = "Add recurring payment",
                        onAction = onAdd,
                        modifier = Modifier.padding(paddingValues).fillMaxSize(),
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.padding(paddingValues).fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(state.items, key = { it.id }) { item ->
                            RecurringTransactionRow(
                                item = item,
                                onClick = { onEdit(item.id) },
                                onDelete = { pendingDeleteId = item.id },
                            )
                        }
                    }
                }
            }
        }
    }

    val deleteId = pendingDeleteId
    if (deleteId != null) {
        ConfirmationDialog(
            title = "Delete this recurring payment?",
            message = "Its schedule stops here — transactions it already recorded aren't affected.",
            confirmLabel = "Delete",
            onConfirm = {
                viewModel.delete(deleteId)
                pendingDeleteId = null
            },
            onDismiss = { pendingDeleteId = null },
        )
    }
}

@Composable
private fun RecurringTransactionRow(item: RecurringTransactionUi, onClick: () -> Unit, onDelete: () -> Unit) {
    val amountColor = if (item.type == TransactionType.INCOME) WalletTheme.extendedColors.income else WalletTheme.extendedColors.expense
    val sign = if (item.type == TransactionType.INCOME) "+" else "-"
    val title = item.payee ?: item.categoryName ?: "Recurring payment"
    val subtitleParts = listOfNotNull(
        item.accountName,
        item.frequency.label(),
        "Next: ${dateGroupLabel(item.nextDate)}",
        if (!item.autoPost) "Reminder only" else null,
    )

    GlassSurface(
        modifier = Modifier.fillMaxWidth(),
        style = GlassStyle.Thin,
        shape = GlassShapes.small,
        elevation = 0.dp,
    ) {
        ListItem(
            modifier = Modifier.clickable(onClick = onClick),
            leadingContent = {
                Icon(
                    imageVector = if (item.autoPost) Icons.Filled.Autorenew else Icons.Filled.NotificationsActive,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            headlineContent = { Text(title, fontSize = 15.sp) },
            supportingContent = { Text(subtitleParts.joinToString(" · "), fontSize = 12.sp) },
            trailingContent = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$sign ${formatMoney(item.amountMinor, item.currency)}",
                        color = amountColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = "Delete $title",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        )
    }
}
