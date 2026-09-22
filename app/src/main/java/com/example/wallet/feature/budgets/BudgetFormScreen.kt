package com.example.wallet.feature.budgets

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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.wallet.core.design.components.DateField
import com.example.wallet.core.design.components.PrimaryButton
import com.example.wallet.core.design.components.SelectorField
import com.example.wallet.core.design.components.SelectorOption
import com.example.wallet.domain.model.BudgetPeriod
import com.example.wallet.domain.model.Category

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetFormScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BudgetFormViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.saved) {
        if (uiState.saved) onSaved()
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.isEditMode) "Edit Budget" else "New Budget") },
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
            OutlinedTextField(
                value = uiState.name,
                onValueChange = viewModel::onNameChange,
                label = { Text("Budget name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            SelectorField(
                label = "Period",
                options = BudgetPeriod.entries.map { SelectorOption(it.name, it.name.lowercase().replaceFirstChar(Char::uppercase)) },
                selectedId = uiState.period.name,
                onSelected = { id -> viewModel.onPeriodChange(BudgetPeriod.valueOf(id)) },
            )

            DateField(dateMillis = uiState.startDate, onDateChange = viewModel::onStartDateChange, label = "Start date")
            DateField(dateMillis = uiState.endDate, onDateChange = viewModel::onEndDateChange, label = "End date")

            OutlinedTextField(
                value = uiState.amountInput,
                onValueChange = viewModel::onAmountChange,
                label = { Text("Budget amount") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            Text(
                text = "Per-category limits (optional)",
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                text = "Leave every category unchecked to track total spending against the budget amount above.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            uiState.expenseCategories.forEach { category ->
                CategoryLimitRow(
                    category = category,
                    isSelected = category.id in uiState.selectedCategoryLimits,
                    limitInput = uiState.selectedCategoryLimits[category.id].orEmpty(),
                    onToggle = { viewModel.onToggleCategory(category.id) },
                    onLimitChange = { value -> viewModel.onCategoryLimitChange(category.id, value) },
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

@Composable
private fun CategoryLimitRow(
    category: Category,
    isSelected: Boolean,
    limitInput: String,
    onToggle: () -> Unit,
    onLimitChange: (String) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = isSelected, onCheckedChange = { onToggle() })
            Text(text = category.name, style = MaterialTheme.typography.bodyLarge)
        }
        if (isSelected) {
            OutlinedTextField(
                value = limitInput,
                onValueChange = onLimitChange,
                label = { Text("${category.name} limit") },
                modifier = Modifier.fillMaxWidth().padding(start = 40.dp, end = 0.dp),
                singleLine = true,
            )
        }
    }
}
