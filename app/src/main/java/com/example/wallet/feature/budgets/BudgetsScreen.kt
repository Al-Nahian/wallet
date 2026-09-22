package com.example.wallet.feature.budgets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.wallet.core.design.components.BudgetProgress
import com.example.wallet.core.design.components.EmptyState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetsScreen(
    onBack: () -> Unit,
    onAddBudget: () -> Unit,
    onBudgetClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BudgetsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Budgets") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onAddBudget) {
                        Icon(Icons.Filled.Add, contentDescription = "New budget")
                    }
                },
            )
        },
    ) { paddingValues ->
        when (val state = uiState) {
            BudgetsUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            is BudgetsUiState.Loaded -> {
                if (state.budgets.isEmpty()) {
                    EmptyState(
                        title = "No budgets yet",
                        subtitle = "Set up a budget to track spending against a limit.",
                        icon = Icons.Filled.PieChart,
                        actionLabel = "New budget",
                        onAction = onAddBudget,
                        modifier = Modifier.padding(paddingValues).fillMaxSize(),
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.padding(paddingValues).fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(state.budgets, key = { it.budget.id }) { summary ->
                            BudgetProgress(
                                name = summary.budget.name,
                                amountMinor = summary.usage.amountMinor,
                                spentMinor = summary.usage.spentMinor,
                                remainingMinor = summary.usage.remainingMinor,
                                usagePercent = summary.usage.usagePercent,
                                currency = summary.budget.currency,
                                modifier = Modifier.clickable { onBudgetClick(summary.budget.id) },
                            )
                        }
                    }
                }
            }
        }
    }
}
