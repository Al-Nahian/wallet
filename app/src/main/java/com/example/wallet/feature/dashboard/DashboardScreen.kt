package com.example.wallet.feature.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.wallet.core.common.formatMoney
import com.example.wallet.core.design.WalletTheme
import com.example.wallet.core.design.components.BalanceCard
import com.example.wallet.core.design.components.EmptyState
import com.example.wallet.core.design.components.IncomeExpenseBar
import com.example.wallet.core.design.components.StatCard
import com.example.wallet.core.design.components.TransactionRow
import com.example.wallet.domain.usecase.dashboard.CategorySpend
import java.util.Locale

/** A fixed rotation of accent colors for the accounts row and category breakdown — a UI variety
 * choice, not a category-taxonomy value, so unlike category colors (which always come from the
 * DB, plan.md §69 rule 8) this is fine to define here. */
private val DashboardAccentPalette = listOf(
    Color(0xFF42B5E8), Color(0xFF9C6ADE), Color(0xFFFF9F1C), Color(0xFF26A69A),
    Color(0xFFEC407A), Color(0xFF7CB342),
)

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
                                BalanceCard(
                                    label = account.name,
                                    amountMinor = account.balanceMinor,
                                    currency = account.currency,
                                    modifier = Modifier.weight(1f),
                                    containerColor = accent.copy(alpha = 0.15f),
                                    contentColor = accent,
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
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        BalanceCard(
                            label = "Income",
                            amountMinor = state.monthlyIncomeMinor,
                            currency = state.currency,
                            modifier = Modifier.weight(1f),
                            containerColor = WalletTheme.extendedColors.income.copy(alpha = 0.15f),
                            contentColor = WalletTheme.extendedColors.income,
                        )
                        BalanceCard(
                            label = "Expense",
                            amountMinor = state.monthlyExpenseMinor,
                            currency = state.currency,
                            modifier = Modifier.weight(1f),
                            containerColor = WalletTheme.extendedColors.expense.copy(alpha = 0.15f),
                            contentColor = WalletTheme.extendedColors.expense,
                        )
                    }
                }
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        IncomeExpenseBar(
                            incomeMinor = state.monthlyIncomeMinor,
                            expenseMinor = state.monthlyExpenseMinor,
                            currency = state.currency,
                            modifier = Modifier.padding(16.dp),
                        )
                    }
                }
                item {
                    BalanceCard(
                        label = "Savings",
                        amountMinor = state.savingsMinor,
                        currency = state.currency,
                        containerColor = WalletTheme.extendedColors.success.copy(alpha = 0.15f),
                        contentColor = WalletTheme.extendedColors.success,
                    )
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        StatCard(
                            label = "Savings Rate",
                            value = String.format(Locale.getDefault(), "%.1f%%", state.savingsRatePercent),
                            modifier = Modifier.weight(1f),
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                        StatCard(
                            label = "Avg. Daily Spend",
                            value = formatMoney(state.averageDailySpendMinor, state.currency),
                            modifier = Modifier.weight(1f),
                            containerColor = WalletTheme.extendedColors.warning.copy(alpha = 0.18f),
                            contentColor = WalletTheme.extendedColors.warning,
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
                    itemsIndexed(state.categorySpend, key = { _, spend -> spend.categoryId ?: "uncategorized" }) { index, spend ->
                        CategorySpendRow(
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

/** A single category's expense share this month: name, amount, percentage, and a proportional
 * bar — deliberately a plain list/bar rather than a full chart library (plan.md's "can be
 * enhanced later" note). [color] rotates per row so the breakdown is visually distinct at a
 * glance, matching the accounts row's palette. */
@Composable
private fun CategorySpendRow(spend: CategorySpend, currency: String, color: Color) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
                Spacer(Modifier.width(6.dp))
                Text(text = spend.categoryName, style = MaterialTheme.typography.bodyMedium)
            }
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
                    .background(color),
            )
        }
    }
}
