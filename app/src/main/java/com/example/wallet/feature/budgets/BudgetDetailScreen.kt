package com.example.wallet.feature.budgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.wallet.core.common.dateGroupLabel
import com.example.wallet.core.design.components.BudgetProgress
import com.example.wallet.core.design.components.GlassScreenTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetDetailScreen(
    onBack: () -> Unit,
    onEdit: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BudgetDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        topBar = {
            GlassScreenTopBar(
                title = "Budget",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    val state = uiState
                    if (state is BudgetDetailUiState.Loaded) {
                        IconButton(onClick = { onEdit(state.budget.id) }) {
                            Icon(Icons.Filled.Edit, contentDescription = "Edit budget")
                        }
                    }
                },
            )
        },
    ) { paddingValues ->
        when (val state = uiState) {
            BudgetDetailUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            BudgetDetailUiState.NotFound -> {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    Text(
                        text = "We couldn't find this budget. It may have been removed.",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(24.dp),
                    )
                }
            }

            is BudgetDetailUiState.Loaded -> {
                LazyColumn(
                    modifier = Modifier.padding(paddingValues).fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    item {
                        BudgetProgress(
                            name = state.budget.name,
                            amountMinor = state.overallUsage.amountMinor,
                            spentMinor = state.overallUsage.spentMinor,
                            remainingMinor = state.overallUsage.remainingMinor,
                            usagePercent = state.overallUsage.usagePercent,
                            currency = state.budget.currency,
                        )
                    }
                    item {
                        Text(
                            text = "${dateGroupLabel(state.budget.startDate)} — ${dateGroupLabel(state.budget.endDate)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    if (state.categoryUsages.isNotEmpty()) {
                        item {
                            Text(text = "Category limits", style = MaterialTheme.typography.titleMedium)
                        }
                        items(state.categoryUsages, key = { it.categoryId }) { categoryUsage ->
                            BudgetProgress(
                                name = categoryUsage.categoryName,
                                amountMinor = categoryUsage.usage.amountMinor,
                                spentMinor = categoryUsage.usage.spentMinor,
                                remainingMinor = categoryUsage.usage.remainingMinor,
                                usagePercent = categoryUsage.usage.usagePercent,
                                currency = state.budget.currency,
                            )
                        }
                    }
                }
            }
        }
    }
}
