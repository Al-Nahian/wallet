package com.example.wallet.feature.automation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import com.example.wallet.core.design.components.GlassScreenScaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.wallet.core.common.formatMoney
import com.example.wallet.core.design.WalletTheme
import com.example.wallet.core.design.components.AccountSelector
import com.example.wallet.core.design.components.CategoryPickerField
import com.example.wallet.core.design.components.EmptyState
import com.example.wallet.core.design.components.GlassScreenTopBar
import com.example.wallet.core.design.components.LiquidGlassCard
import com.example.wallet.core.design.components.PrimaryButton
import com.example.wallet.core.design.components.SecondaryButton
import com.example.wallet.core.design.components.SelectorOption
import com.example.wallet.core.design.glass.GlassColors
import com.example.wallet.core.design.glass.GlassShapes
import com.example.wallet.domain.model.AutomationCandidate
import com.example.wallet.domain.model.Category
import com.example.wallet.domain.model.CategoryGroup
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

    GlassScreenScaffold(
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
            EmptyState(
                title = "Nothing needs review",
                subtitle = "Detected transactions that need your confirmation will show up here.",
                icon = Icons.Filled.TaskAlt,
                modifier = Modifier.padding(paddingValues).fillMaxSize(),
            )
            return@GlassScreenScaffold
        }

        Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            if (uiState.errorMessage != null) {
                Text(
                    text = uiState.errorMessage.orEmpty(),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
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
    accountOptions: List<SelectorOption>,
    categoryGroups: List<CategoryGroup>,
    categories: List<Category>,
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

    LiquidGlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = GlassShapes.medium,
        tint = GlassColors.neutralGlassTint(WalletTheme.extendedColors.transfer),
        lightweight = true,
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = formatMoney(candidate.amountMinor, candidate.currency),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                )
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

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
            ) {
                SecondaryButton(text = "Ignore", onClick = onIgnore, modifier = Modifier.weight(1f))
                PrimaryButton(text = "Accept", onClick = onAccept, enabled = canAccept, modifier = Modifier.weight(1f))
            }
        }
    }
}
