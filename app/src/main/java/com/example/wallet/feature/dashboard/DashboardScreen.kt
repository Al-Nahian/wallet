package com.example.wallet.feature.dashboard

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.wallet.core.design.components.EmptyState

@Composable
fun DashboardScreen(modifier: Modifier = Modifier) {
    EmptyState(
        title = "Your dashboard will live here",
        subtitle = "Balance, income, expenses and insights are coming in a later phase.",
        icon = Icons.Filled.Home,
        modifier = modifier,
    )
}
