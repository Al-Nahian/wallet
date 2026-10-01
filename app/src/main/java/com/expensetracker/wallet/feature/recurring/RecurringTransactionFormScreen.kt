package com.expensetracker.wallet.feature.recurring

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import com.expensetracker.wallet.core.design.components.GlassScreenScaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.expensetracker.wallet.core.design.components.AccountSelector
import com.expensetracker.wallet.core.design.components.AmountInput
import com.expensetracker.wallet.core.design.components.CategoryPickerField
import com.expensetracker.wallet.core.design.components.DateField
import com.expensetracker.wallet.core.design.components.GlassScreenTopBar
import com.expensetracker.wallet.core.design.components.PrimaryButton
import com.expensetracker.wallet.core.design.components.SelectorField
import com.expensetracker.wallet.core.design.components.SelectorOption
import com.expensetracker.wallet.domain.model.RecurringFrequency
import com.expensetracker.wallet.domain.model.TransactionType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurringTransactionFormScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RecurringTransactionFormViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.saved) {
        if (uiState.saved) onSaved()
    }

    GlassScreenScaffold(
        modifier = modifier,
        topBar = {
            GlassScreenTopBar(
                title = if (uiState.isEditMode) "Edit Recurring Payment" else "New Recurring Payment",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@GlassScreenScaffold
        }

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            RecurringTypeToggle(selected = uiState.type, onSelected = viewModel::onTypeChange)

            AmountInput(value = uiState.amountInput, onValueChange = viewModel::onAmountChange)

            AccountSelector(
                accounts = uiState.accountOptions,
                selectedAccountId = uiState.accountId,
                onSelected = viewModel::onAccountChange,
            )

            CategoryPickerField(
                groups = uiState.categoryGroups,
                categories = uiState.categories,
                selectedCategoryId = uiState.categoryId,
                onSelected = viewModel::onCategoryChange,
            )

            OutlinedTextField(
                value = uiState.payee,
                onValueChange = viewModel::onPayeeChange,
                label = { Text("Payee (optional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            SelectorField(
                label = "Repeats",
                options = RecurringFrequency.entries.map { SelectorOption(it.name, it.label()) },
                selectedId = uiState.frequency.name,
                onSelected = { id -> viewModel.onFrequencyChange(RecurringFrequency.valueOf(id)) },
            )

            DateField(dateMillis = uiState.nextDate, onDateChange = viewModel::onNextDateChange, label = "Next due date")

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(text = "Set an end date", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                Switch(checked = uiState.hasEndDate, onCheckedChange = viewModel::onHasEndDateChange)
            }
            if (uiState.hasEndDate) {
                DateField(dateMillis = uiState.endDate, onDateChange = viewModel::onEndDateChange, label = "Ends on")
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = "When it's due", style = MaterialTheme.typography.titleSmall)
                Text(
                    text = "Record it automatically, or just get a reminder and enter it yourself.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 4.dp)) {
                    FilterChip(
                        selected = uiState.autoPost,
                        onClick = { viewModel.onAutoPostChange(true) },
                        label = { Text("Record automatically") },
                    )
                    FilterChip(
                        selected = !uiState.autoPost,
                        onClick = { viewModel.onAutoPostChange(false) },
                        label = { Text("Just remind me") },
                    )
                }
            }

            OutlinedTextField(
                value = uiState.note,
                onValueChange = viewModel::onNoteChange,
                label = { Text("Note (optional)") },
                modifier = Modifier.fillMaxWidth(),
            )

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

/** Recurring rules can only be an expense or income (domain/usecase/recurring's
 * `RecurringTransactionError.InvalidType`) — a transfer has no single account/category shape to
 * recur against, so there's no Transfer option here, unlike the plain transaction form's toggle. */
@Composable
private fun RecurringTypeToggle(selected: TransactionType, onSelected: (TransactionType) -> Unit) {
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
    }
}
