package com.example.wallet.feature.transactions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.wallet.core.common.minorUnitsToEditableString
import com.example.wallet.core.common.parseMoneyToMinorUnits
import com.example.wallet.core.design.WalletTheme
import com.example.wallet.core.design.components.AccountSelector
import com.example.wallet.core.design.components.AmountInput
import com.example.wallet.core.design.components.CategoryPickerField
import com.example.wallet.core.design.components.DateField
import com.example.wallet.core.design.components.LabelChip
import com.example.wallet.core.design.components.PrimaryButton
import com.example.wallet.core.design.components.SecondaryButton
import com.example.wallet.domain.model.Category
import com.example.wallet.domain.model.CategoryGroup
import com.example.wallet.domain.model.Label
import com.example.wallet.domain.model.TransactionType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionFormScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TransactionFormViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.saved) {
        if (uiState.saved) onSaved()
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.isEditMode) "Edit Transaction" else "New Transaction") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            TransactionTypeToggle(
                selected = uiState.type,
                isEditMode = uiState.isEditMode,
                onSelected = viewModel::onTypeChange,
            )

            AmountInput(value = uiState.amountInput, onValueChange = viewModel::onAmountChange)

            if (uiState.type == TransactionType.TRANSFER) {
                AccountSelector(
                    accounts = uiState.accountOptions,
                    selectedAccountId = uiState.accountId,
                    onSelected = viewModel::onAccountChange,
                    label = "From Account",
                )
                AccountSelector(
                    accounts = uiState.accountOptions.filter { it.id != uiState.accountId },
                    selectedAccountId = uiState.toAccountId,
                    onSelected = viewModel::onToAccountChange,
                    label = "To Account",
                )
            } else {
                AccountSelector(
                    accounts = uiState.accountOptions,
                    selectedAccountId = uiState.accountId,
                    onSelected = viewModel::onAccountChange,
                )

                if (!uiState.isSplitEnabled) {
                    CategoryPickerField(
                        groups = uiState.categoryGroups,
                        categories = uiState.categories,
                        selectedCategoryId = uiState.categoryId,
                        onSelected = viewModel::onCategoryChange,
                    )
                }

                OutlinedTextField(
                    value = uiState.payee,
                    onValueChange = viewModel::onPayeeChange,
                    label = { Text("Payee (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            DateField(dateMillis = uiState.date, onDateChange = viewModel::onDateChange)

            OutlinedTextField(
                value = uiState.note,
                onValueChange = viewModel::onNoteChange,
                label = { Text("Note (optional)") },
                modifier = Modifier.fillMaxWidth(),
            )

            if (uiState.type != TransactionType.TRANSFER) {
                SecondaryButton(
                    text = if (uiState.isSplitEnabled) "Remove split" else "Split this transaction",
                    onClick = { viewModel.onToggleSplit(!uiState.isSplitEnabled) },
                    modifier = Modifier.fillMaxWidth(),
                )

                if (uiState.isSplitEnabled) {
                    SplitEditor(
                        rows = uiState.splitRows,
                        categoryGroups = uiState.categoryGroups,
                        categories = uiState.categories,
                        targetAmountInput = uiState.amountInput,
                        onAddRow = viewModel::addSplitRow,
                        onRemoveRow = viewModel::removeSplitRow,
                        onCategoryChange = viewModel::onSplitCategoryChange,
                        onAmountChange = viewModel::onSplitAmountChange,
                        onNoteChange = viewModel::onSplitNoteChange,
                    )
                }

                LabelPickerSection(
                    allLabels = uiState.labelOptions,
                    selectedLabelIds = uiState.selectedLabelIds,
                    onToggleLabel = viewModel::onToggleLabel,
                )
            }

            if (uiState.errorMessage != null) {
                Text(
                    text = uiState.errorMessage.orEmpty(),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            PrimaryButton(
                text = if (uiState.isSaving) "Saving..." else "Save",
                onClick = viewModel::save,
                enabled = !uiState.isSaving,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/** plan.md §10/§22 — Expense/Income/Transfer as one toggle. Transfer can't be switched to (or
 * from) in edit mode: transfers have no edit support yet, and an existing expense/income can't
 * be converted into a transfer's two linked rows via this same edit flow. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TransactionTypeToggle(selected: TransactionType, isEditMode: Boolean, onSelected: (TransactionType) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(
            selected = selected == TransactionType.EXPENSE,
            onClick = { onSelected(TransactionType.EXPENSE) },
            label = { Text("Expense") },
        )
        FilterChip(
            selected = selected == TransactionType.INCOME,
            onClick = { onSelected(TransactionType.INCOME) },
            label = { Text("Income") },
        )
        if (!isEditMode) {
            FilterChip(
                selected = selected == TransactionType.TRANSFER,
                onClick = { onSelected(TransactionType.TRANSFER) },
                label = { Text("Transfer") },
            )
        }
    }
}

/** plan.md §13 split entry: category + amount rows plus a running total vs. the transaction's
 * own amount, so a mismatched split is visible before Save is even tapped. */
@Composable
private fun SplitEditor(
    rows: List<SplitRowState>,
    categoryGroups: List<CategoryGroup>,
    categories: List<Category>,
    targetAmountInput: String,
    onAddRow: () -> Unit,
    onRemoveRow: (String) -> Unit,
    onCategoryChange: (String, String) -> Unit,
    onAmountChange: (String, String) -> Unit,
    onNoteChange: (String, String) -> Unit,
) {
    val targetMinor = parseMoneyToMinorUnits(targetAmountInput) ?: 0L
    val runningTotal = rows.sumOf { parseMoneyToMinorUnits(it.amountInput) ?: 0L }
    val isBalanced = runningTotal == targetMinor

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        rows.forEach { row ->
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    CategoryPickerField(
                        groups = categoryGroups,
                        categories = categories,
                        selectedCategoryId = row.categoryId,
                        onSelected = { id -> id?.let { onCategoryChange(row.key, it) } },
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = { onRemoveRow(row.key) }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Remove split")
                    }
                }
                AmountInput(
                    value = row.amountInput,
                    onValueChange = { onAmountChange(row.key, it) },
                    label = "Split amount",
                )
                OutlinedTextField(
                    value = row.note,
                    onValueChange = { onNoteChange(row.key, it) },
                    label = { Text("Note (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        SecondaryButton(text = "Add split", onClick = onAddRow, modifier = Modifier.fillMaxWidth())

        Text(
            text = "Split total: ${minorUnitsToEditableString(runningTotal)} of ${minorUnitsToEditableString(targetMinor)}",
            style = MaterialTheme.typography.bodyMedium,
            color = if (isBalanced) WalletTheme.extendedColors.income else MaterialTheme.colorScheme.error,
        )
    }
}

/** plan.md §16: multi-select label picker — selected labels show as removable chips, with a
 * dialog listing every label as a checkbox row to add/remove from the selection. */
@Composable
private fun LabelPickerSection(
    allLabels: List<Label>,
    selectedLabelIds: Set<String>,
    onToggleLabel: (String) -> Unit,
) {
    var showPicker by remember { mutableStateOf(false) }
    val selectedLabels = allLabels.filter { it.id in selectedLabelIds }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = "Labels", style = MaterialTheme.typography.labelLarge)
        if (selectedLabels.isNotEmpty()) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.horizontalScroll(rememberScrollState()),
            ) {
                selectedLabels.forEach { label ->
                    LabelChip(name = label.name, color = label.color, onRemove = { onToggleLabel(label.id) })
                }
            }
        }
        SecondaryButton(text = "Choose labels", onClick = { showPicker = true }, modifier = Modifier.fillMaxWidth())
    }

    if (showPicker) {
        AlertDialog(
            onDismissRequest = { showPicker = false },
            title = { Text("Labels") },
            text = {
                if (allLabels.isEmpty()) {
                    Text("No labels yet. Create some from Profile > Manage labels.")
                } else {
                    Column {
                        allLabels.forEach { label ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .clickable { onToggleLabel(label.id) },
                            ) {
                                Checkbox(checked = label.id in selectedLabelIds, onCheckedChange = { onToggleLabel(label.id) })
                                Spacer(Modifier.width(8.dp))
                                LabelChip(name = label.name, color = label.color)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPicker = false }) { Text("Done") }
            },
        )
    }
}

