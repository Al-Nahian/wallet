package com.example.wallet.feature.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.wallet.core.common.formatMoney
import com.example.wallet.core.design.components.AccountSummaryCard
import com.example.wallet.core.design.components.BalanceCard
import com.example.wallet.core.design.components.BudgetProgress
import com.example.wallet.core.design.components.CashFlowCard
import com.example.wallet.core.design.components.CategoryBreakdownRow
import com.example.wallet.core.design.components.EmptyState
import com.example.wallet.core.design.components.StatCard
import com.example.wallet.core.design.components.TransactionRow
import com.example.wallet.feature.accounts.icon
import java.util.Locale

/** A fixed rotation of accent colors for the accounts row and category breakdown — a UI variety
 * choice, not a category-taxonomy value, so unlike category colors (which always come from the
 * DB, plan.md §69 rule 8) this is fine to define here. */
private val DashboardAccentPalette = listOf(
    Color(0xFF42B5E8), Color(0xFF9C6ADE), Color(0xFFFF9F1C), Color(0xFF26A69A),
    Color(0xFFEC407A), Color(0xFF7CB342),
)

/** Solid, full-opacity card colors for the dashboard's headline metrics — fixed (not
 * theme-tinted) so each card reads clearly against either a light or dark page background,
 * mirroring the accounts grid's already-approved solid-tile look. */
private val TotalBalanceColor = Color(0xFF5B4FE0)
private val SavingsColor = Color(0xFF16A34A)
private val SavingsRateColor = Color(0xFF0D9488)
private val AvgDailySpendColor = Color(0xFFEA580C)

@Composable
fun DashboardScreen(
    onTransactionClick: (String) -> Unit,
    onSeeAllTransactions: () -> Unit,
    onManageBudgets: () -> Unit,
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
                item { SectionHeader("Accounts") }
                if (state.accountBalances.isEmpty()) {
                    item {
                        Text(
                            text = "No accounts yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    val accountRows = state.accountBalances.chunked(2)
                    items(accountRows, key = { row -> row.joinToString { it.id } }) { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                            row.forEach { account ->
                                val accent = DashboardAccentPalette[state.accountBalances.indexOf(account) % DashboardAccentPalette.size]
                                AccountSummaryCard(
                                    label = account.name,
                                    amountMinor = account.balanceMinor,
                                    currency = account.currency,
                                    icon = account.type.icon(),
                                    backgroundColor = accent,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                            if (row.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }

                item {
                    BalanceCard(
                        label = "Total Balance",
                        amountMinor = state.totalBalanceMinor,
                        currency = state.currency,
                        containerColor = TotalBalanceColor,
                        contentColor = Color.White,
                    )
                }
                item {
                    CashFlowCard(
                        periodLabel = "This Month",
                        incomeMinor = state.monthlyIncomeMinor,
                        expenseMinor = state.monthlyExpenseMinor,
                        currency = state.currency,
                        previousNetMinor = state.previousMonthNetMinor,
                    )
                }
                item {
                    BalanceCard(
                        label = "Savings",
                        amountMinor = state.savingsMinor,
                        currency = state.currency,
                        containerColor = SavingsColor,
                        contentColor = Color.White,
                    )
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        StatCard(
                            label = "Savings Rate",
                            value = String.format(Locale.getDefault(), "%.1f%%", state.savingsRatePercent),
                            modifier = Modifier.weight(1f),
                            containerColor = SavingsRateColor,
                            contentColor = Color.White,
                        )
                        StatCard(
                            label = "Avg. Daily Spend",
                            value = formatMoney(state.averageDailySpendMinor, state.currency),
                            modifier = Modifier.weight(1f),
                            containerColor = AvgDailySpendColor,
                            contentColor = Color.White,
                        )
                    }
                }

                item { SectionHeader("Monthly Budget") }
                if (state.activeBudgets.isEmpty()) {
                    item {
                        EmptyState(
                            title = "No budgets yet",
                            subtitle = "Set up a budget to see how you're tracking here.",
                            icon = Icons.Filled.PieChart,
                            actionLabel = "Set up a budget",
                            onAction = onManageBudgets,
                            modifier = Modifier.height(200.dp),
                        )
                    }
                } else {
                    items(state.activeBudgets, key = { it.budget.id }) { summary ->
                        BudgetProgress(
                            name = summary.budget.name,
                            amountMinor = summary.usage.amountMinor,
                            spentMinor = summary.usage.spentMinor,
                            remainingMinor = summary.usage.remainingMinor,
                            usagePercent = summary.usage.usagePercent,
                            currency = summary.budget.currency,
                            modifier = Modifier.clickable(onClick = onManageBudgets),
                        )
                    }
                    item {
                        Text(
                            text = "Manage budgets",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().clickable(onClick = onManageBudgets).padding(8.dp),
                        )
                    }
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
                    itemsIndexed(state.categorySpend, key = { _, spend -> spend.categoryId ?: "uncategorized" }) { index, spend ->
                        CategoryBreakdownRow(
                            spend = spend,
                            currency = state.currency,
                            color = DashboardAccentPalette[index % DashboardAccentPalette.size],
                        )
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

