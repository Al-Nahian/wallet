package com.example.wallet.feature.accounts

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.wallet.core.design.components.PrimaryButton
import com.example.wallet.domain.model.AccountType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountFormScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AccountFormViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.saved) {
        if (uiState.saved) onSaved()
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.isEditMode) "Edit Account" else "New Account") },
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
            OutlinedTextField(
                value = uiState.name,
                onValueChange = viewModel::onNameChange,
                label = { Text("Account name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            AccountTypeDropdown(
                selected = uiState.type,
                onSelected = viewModel::onTypeChange,
            )

            OutlinedTextField(
                value = uiState.institutionName,
                onValueChange = viewModel::onInstitutionChange,
                label = { Text("Institution (optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            if (!uiState.isEditMode) {
                OutlinedTextField(
                    value = uiState.currency,
                    onValueChange = viewModel::onCurrencyChange,
                    label = { Text("Currency") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )

                OutlinedTextField(
                    value = uiState.openingBalanceInput,
                    onValueChange = viewModel::onOpeningBalanceChange,
                    label = { Text("Opening balance") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
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
private fun AccountTypeDropdown(
    selected: AccountType,
    onSelected: (AccountType) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = selected.label(),
            onValueChange = {},
            readOnly = true,
            label = { Text("Account type") },
            trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
        )
        // Transparent overlay so tapping the (read-only) field toggles the menu instead
        // of focusing it for text input.
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { expanded = true },
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(),
        ) {
            AccountType.entries.forEach { type ->
                DropdownMenuItem(
                    text = { Text(type.label()) },
                    onClick = {
                        onSelected(type)
                        expanded = false
                    },
                )
            }
        }
    }
}
