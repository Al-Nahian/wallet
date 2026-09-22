package com.example.wallet.feature.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.wallet.core.common.formatMoney
import com.example.wallet.core.design.WalletTheme
import com.example.wallet.core.design.components.BalanceCard
import com.example.wallet.core.design.components.EmptyState
import com.example.wallet.core.design.components.StatCard
import com.example.wallet.core.design.components.TransactionRow
import com.example.wallet.domain.usecase.dashboard.CategorySpend
import java.util.Locale

@Composable
fun DashboardScreen(
    onTransactionClick: (String) -> Unit,
    onSeeAllTransactions: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val state = uiState) {
        DashboardUiState.Loading -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        is DashboardUiState.Loaded -> {
            LazyColumn(
                modifier = modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item {
                    BalanceCard(label = "Total Balance", amountMinor = state.totalBalanceMinor, currency = state.currency)
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        BalanceCard(
                            label = "Income",
                            amountMinor = state.monthlyIncomeMinor,
                            currency = state.currency,
                            modifier = Modifier.weight(1f),
                        )
                        BalanceCard(
                            label = "Expense",
                            amountMinor = state.monthlyExpenseMinor,
                            currency = state.currency,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                item {
                    BalanceCard(label = "Savings", amountMinor = state.savingsMinor, currency = state.currency)
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        StatCard(
                            label = "Savings Rate",
                            value = String.format(Locale.getDefault(), "%.1f%%", state.savingsRatePercent),
                            modifier = Modifier.weight(1f),
                        )
                        StatCard(
                            label = "Avg. Daily Spend",
                            value = formatMoney(state.averageDailySpendMinor, state.currency),
                            modifier = Modifier.weight(1f),
                        )
                    }
                }

                item { SectionHeader("Monthly Budget") }
                item {
                    EmptyState(
                        title = "No budgets yet",
                        subtitle = "Set up a budget to see how you're tracking here.",
                        icon = Icons.Filled.PieChart,
                        modifier = Modifier.height(160.dp),
                    )
                }

                item { SectionHeader("Spending by Category") }
                if (state.categorySpend.isEmpty()) {
                    item {
                        Text(
                            text = "No expenses recorded this month yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    items(state.categorySpend, key = { it.categoryId ?: "uncategorized" }) { spend ->
                        CategorySpendRow(spend = spend, currency = state.currency)
                    }
                }

                item { SectionHeader("Recent Transactions") }
                if (state.recentTransactions.isEmpty()) {
                    item {
                        EmptyState(
                            title = "No transactions yet",
                            subtitle = "Transactions you record show up here.",
                            icon = Icons.Filled.Receipt,
                            modifier = Modifier.height(160.dp),
                        )
                    }
                } else {
                    items(state.recentTransactions, key = { it.id }) { transaction ->
                        TransactionRow(
                            title = transaction.title,
                            subtitle = transaction.subtitle,
                            amountMinor = transaction.amountMinor,
                            currency = transaction.currency,
                            isIncome = transaction.isIncome,
                            isTransfer = transaction.isTransfer,
                            onClick = { onTransactionClick(transaction.id) },
                            onDelete = {},
                        )
                    }
                    item {
                        Text(
                            text = "See all transactions",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().clickable(onClick = onSeeAllTransactions).padding(8.dp),
                        )
                    }
                }

                item { SectionHeader("Upcoming Recurring Payments") }
                item {
                    EmptyState(
                        title = "Nothing scheduled",
                        subtitle = "Recurring payments will show up here once you set them up.",
                        icon = Icons.Filled.CalendarMonth,
                        modifier = Modifier.height(160.dp),
                    )
                }

                item { SectionHeader("Goals") }
                item {
                    EmptyState(
                        title = "No goals yet",
                        subtitle = "Set a savings goal to track your progress here.",
                        icon = Icons.Filled.Flag,
                        modifier = Modifier.height(160.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(text = title, style = MaterialTheme.typography.titleMedium)
}

/** A single category's expense share this month: name, amount, percentage, and a proportional
 * bar — deliberately a plain list/bar rather than a full chart library (plan.md's "can be
 * enhanced later" note). */
@Composable
private fun CategorySpendRow(spend: CategorySpend, currency: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text(text = spend.categoryName, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = "${formatMoney(spend.amountMinor, currency)} (${String.format(Locale.getDefault(), "%.0f%%", spend.percentage)})",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth((spend.percentage / 100.0).toFloat().coerceIn(0f, 1f))
                    .fillMaxSize()
                    .clip(RoundedCornerShape(4.dp))
                    .background(WalletTheme.extendedColors.expense),
            )
        }
    }
}
