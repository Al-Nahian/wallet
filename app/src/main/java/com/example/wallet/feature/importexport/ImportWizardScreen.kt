package com.example.wallet.feature.importexport

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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import com.example.wallet.core.design.components.GlassScreenScaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.wallet.core.design.components.GlassScreenTopBar
import com.example.wallet.core.design.components.PrimaryButton
import com.example.wallet.core.design.components.SelectorField
import com.example.wallet.core.design.components.SelectorOption
import com.example.wallet.domain.usecase.importexport.ImportColumn
import com.example.wallet.domain.usecase.importexport.ParsedImportRow

private const val NONE_OPTION_ID = "-1"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportWizardScreen(
    onBack: () -> Unit,
    onImported: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ImportWizardViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.committed) {
        if (uiState.committed) onImported()
    }

    GlassScreenScaffold(
        modifier = modifier,
        topBar = {
            GlassScreenTopBar(
                title = "Import CSV",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { paddingValues ->
        when (uiState.step) {
            WizardStep.LOADING -> Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator() }

            WizardStep.ERROR -> Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues).padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = uiState.errorMessage ?: "Something went wrong.",
                    color = MaterialTheme.colorScheme.error,
                )
            }

            WizardStep.MAPPING -> MappingStep(
                headers = uiState.headers,
                mapping = uiState.mapping,
                errorMessage = uiState.errorMessage,
                onMappingChange = viewModel::onMappingChange,
                onContinue = viewModel::confirmMapping,
                modifier = Modifier.padding(paddingValues),
            )

            WizardStep.PREVIEW -> PreviewStep(
                rows = uiState.rows,
                isCommitting = uiState.isCommitting,
                onToggleRow = viewModel::toggleRowAccepted,
                onCommit = viewModel::commit,
                modifier = Modifier.padding(paddingValues),
            )
        }
    }
}

@Composable
private fun MappingStep(
    headers: List<String>,
    mapping: Map<ImportColumn, Int?>,
    errorMessage: String?,
    onMappingChange: (ImportColumn, Int?) -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val headerOptions = listOf(SelectorOption(NONE_OPTION_ID, "Not in file")) +
        headers.mapIndexed { index, header -> SelectorOption(index.toString(), header) }

    Column(
        modifier = modifier.padding(16.dp).fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(text = "Match each field to a column in your file.", style = MaterialTheme.typography.bodyMedium)
        ImportColumn.entries.forEach { column ->
            SelectorField(
                label = column.label,
                options = headerOptions,
                selectedId = (mapping[column] ?: -1).toString(),
                onSelected = { id ->
                    val index = id.toIntOrNull()?.takeIf { it >= 0 }
                    onMappingChange(column, index)
                },
            )
        }
        if (errorMessage != null) {
            Text(text = errorMessage, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
        }
        PrimaryButton(text = "Continue", onClick = onContinue, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun PreviewStep(
    rows: List<ParsedImportRow>,
    isCommitting: Boolean,
    onToggleRow: (Int) -> Unit,
    onCommit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val acceptedCount = rows.count { it.accepted && it.errors.isEmpty() }

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "$acceptedCount of ${rows.size} rows will be imported.",
            style = MaterialTheme.typography.titleSmall,
        )
        LazyColumn(modifier = Modifier.weight(1f).padding(top = 8.dp)) {
            items(rows, key = { it.rowIndex }) { row -> PreviewRow(row = row, onToggle = { onToggleRow(row.rowIndex) }) }
        }
        PrimaryButton(
            text = if (isCommitting) "Importing..." else "Import $acceptedCount rows",
            onClick = onCommit,
            enabled = !isCommitting && acceptedCount > 0,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        )
    }
}

@Composable
private fun PreviewRow(row: ParsedImportRow, onToggle: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Checkbox(checked = row.accepted, onCheckedChange = { onToggle() }, enabled = row.errors.isEmpty())
        Column(modifier = Modifier.padding(start = 4.dp)) {
            Text(
                text = "${row.accountName ?: "?"} · ${row.type?.name ?: "?"} · " +
                    (row.categoryName ?: row.labelName ?: row.payee ?: "(uncategorized)"),
                style = MaterialTheme.typography.bodyLarge,
            )
            if (row.errors.isNotEmpty()) {
                Text(
                    text = row.errors.joinToString(", "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            } else if (row.isDuplicate) {
                Text(
                    text = "Looks like a duplicate — unchecked by default",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.tertiary,
                )
            }
        }
    }
}
