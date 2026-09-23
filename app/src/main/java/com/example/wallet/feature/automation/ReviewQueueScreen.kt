package com.example.wallet.feature.automation

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
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.wallet.core.common.formatMoney
import com.example.wallet.core.design.components.AccountSelector
import com.example.wallet.core.design.components.CategoryPickerField
import com.example.wallet.core.design.components.GlassScreenTopBar
import com.example.wallet.core.design.components.PrimaryButton
import com.example.wallet.core.design.components.SecondaryButton
import com.example.wallet.domain.model.AutomationCandidate
import com.example.wallet.domain.model.ParseConfidence
import com.example.wallet.domain.model.TransactionType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewQueueScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ReviewQueueViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        topBar = {
            GlassScreenTopBar(
                title = "Review Queue",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { paddingValues ->
        if (uiState.candidates.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues).padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Nothing needs review right now.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            return@Scaffold
        }

        Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            if (uiState.errorMessage != null) {
                Text(
                    text = uiState.errorMessage.orEmpty(),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp),
                )
            }
            LazyColumn(
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(uiState.candidates, key = { it.id }) { candidate ->
                    CandidateCard(
                        candidate = candidate,
                        accountOptions = uiState.accountOptions,
                        categoryGroups = uiState.categoryGroups,
                        categories = uiState.categories,
                        selectedAccountId = uiState.accountIdFor(candidate),
                        selectedToAccountId = uiState.toAccountIdFor(candidate),
                        selectedCategoryId = uiState.categoryIdFor(candidate),
                        onAccountChange = { id -> viewModel.onAccountChange(candidate.id, id) },
                        onToAccountChange = { id -> viewModel.onToAccountChange(candidate.id, id) },
                        onCategoryChange = { id -> viewModel.onCategoryChange(candidate.id, id) },
                        onAccept = { viewModel.accept(candidate) },
                        onIgnore = { viewModel.ignore(candidate.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun CandidateCard(
    candidate: AutomationCandidate,
    accountOptions: List<com.example.wallet.core.design.components.SelectorOption>,
    categoryGroups: List<com.example.wallet.domain.model.CategoryGroup>,
    categories: List<com.example.wallet.domain.model.Category>,
    selectedAccountId: String?,
    selectedToAccountId: String?,
    selectedCategoryId: String?,
    onAccountChange: (String) -> Unit,
    onToAccountChange: (String) -> Unit,
    onCategoryChange: (String?) -> Unit,
    onAccept: () -> Unit,
    onIgnore: () -> Unit,
) {
    val isTransfer = candidate.type == TransactionType.TRANSFER
    val canAccept = selectedAccountId != null && (!isTransfer || selectedToAccountId != null)

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(text = formatMoney(candidate.amountMinor, candidate.currency), style = MaterialTheme.typography.titleMedium)
                if (candidate.confidence == ParseConfidence.HIGH) {
                    Text(
                        text = "High confidence",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            Text(
                text = "${candidate.type.name.lowercase().replaceFirstChar { it.uppercase() }}" +
                    (candidate.payee?.let { " · $it" } ?: ""),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            AccountSelector(
                accounts = accountOptions,
                selectedAccountId = selectedAccountId,
                onSelected = onAccountChange,
                label = if (isTransfer) "From account" else "Account",
            )

            if (isTransfer) {
                AccountSelector(
                    accounts = accountOptions,
                    selectedAccountId = selectedToAccountId,
                    onSelected = onToAccountChange,
                    label = "To account",
                )
            } else {
                CategoryPickerField(
                    groups = categoryGroups,
                    categories = categories,
                    selectedCategoryId = selectedCategoryId,
                    onSelected = onCategoryChange,
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                SecondaryButton(text = "Ignore", onClick = onIgnore, modifier = Modifier.weight(1f))
                PrimaryButton(text = "Accept", onClick = onAccept, enabled = canAccept, modifier = Modifier.weight(1f))
            }
        }
    }
}
