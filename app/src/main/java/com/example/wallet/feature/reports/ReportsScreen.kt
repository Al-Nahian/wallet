package com.example.wallet.feature.reports

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.wallet.core.design.components.EmptyState

@Composable
fun ReportsScreen(modifier: Modifier = Modifier) {
    EmptyState(
        title = "Reports will live here",
        subtitle = "Spending, income and cash-flow reports arrive in Phase 8.",
        icon = Icons.Filled.BarChart,
        modifier = modifier,
    )
}
