package com.example.wallet.feature.dashboard

import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.wallet.core.common.dateGroupLabel
import com.example.wallet.core.common.formatMoney
import com.example.wallet.core.design.WalletTheme
import com.example.wallet.core.design.components.AccountSummaryCard
import com.example.wallet.core.design.components.BalanceCard
import com.example.wallet.core.design.components.BudgetProgress
import com.example.wallet.core.design.components.CashFlowCard
import com.example.wallet.core.design.components.CategoryBreakdownRow
import com.example.wallet.core.design.components.EmptyState
import com.example.wallet.core.design.components.LiquidGlassCard
import com.example.wallet.core.design.components.StatCard
import com.example.wallet.core.design.components.TransactionRow
import com.example.wallet.core.design.components.WalletBottomNavSpace
import com.example.wallet.core.design.glass.GlassColors
import com.example.wallet.core.design.glass.GlassShapes
import com.example.wallet.domain.model.TransactionType
import com.example.wallet.feature.accounts.icon
import com.example.wallet.feature.recurring.RecurringTransactionUi
import com.example.wallet.feature.recurring.label
import java.util.Locale

/** A fixed rotation of accent colors for the accounts row and category breakdown — a UI variety
 * choice, not a category-taxonomy value, so unlike category colors (which always come from the
 * DB, plan.md §69 rule 8) this is fine to define here. */
private val DashboardAccentPalette = listOf(
    Color(0xFF42B5E8), Color(0xFF9C6ADE), Color(0xFFFF9F1C), Color(0xFF26A69A),
    Color(0xFFEC407A), Color(0xFF7CB342),
)

/** Fixed (not theme-tinted) card colors for the dashboard's headline metrics, so each card reads
 * clearly against either a light or dark page background — brighter/more saturated than a plain
 * Material tone, since these sit behind [GlassTokens.frostedFillAlpha] translucency and need to
 * still read as vivid rather than washed out. */
private val TotalBalanceColor = Color(0xFF6C5CE7)
private val SavingsColor = Color(0xFF22C55E)
private val SavingsRateColor = Color(0xFF14B8A6)

/** A distinct red rather than the previous muted orange — spend should read as attention-getting,
 * not blend in with the neutral palette. */
private val AvgDailySpendColor = Color(0xFFEF4444)

@Composable
fun DashboardScreen(
    onTransactionClick: (String) -> Unit,
    onSeeAllTransactions: () -> Unit,
    onManageBudgets: () -> Unit,
    onManageRecurring: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    when (val state = uiState) {
        DashboardUiState.Loading -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        is DashboardUiState.Loaded -> {
            LazyColumn(
                modifier = modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 16.dp,
                    bottom = 16.dp + WalletBottomNavSpace,
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp),
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
                    // One LazyColumn item holding every account row, with its own tighter
                    // vertical spacing — the outer LazyColumn's 16dp spacedBy is a section-to-
                    // section gap (Accounts → Total Balance, etc.), which read as too loose
                    // *within* this one section's own rows.
                    item {
                        val accountRows = state.accountBalances.chunked(2)
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            accountRows.forEach { row ->
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                                    row.forEach { account ->
                                        val index = state.accountBalances.indexOf(account)
                                        val accent = DashboardAccentPalette[index % DashboardAccentPalette.size]
                                        AccountSummaryCard(
                                            label = account.name,
                                            amountMinor = account.balanceMinor,
                                            currency = account.currency,
                                            icon = account.type.icon(),
                                            backgroundColor = accent,
                                            // A lone odd-one-out in the last row spreads to fill the
                                            // row (like Total Balance below) instead of leaving an
                                            // empty slot beside it where its pair would have gone.
                                            modifier = Modifier.weight(1f),
                                            // Explicit rather than hash-derived: neighbors in the
                                            // same row otherwise risk landing on the same wave
                                            // shape by coincidence.
                                            waveVariant = index % 5,
                                        )
                                    }
                                }
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
                        icon = Icons.Filled.AccountBalance,
                        waveVariant = 0,
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
                        icon = Icons.Filled.Savings,
                        waveVariant = 1,
                    )
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                        StatCard(
                            label = "Savings Rate",
                            value = String.format(Locale.getDefault(), "%.1f%%", state.savingsRatePercent),
                            modifier = Modifier.weight(1f),
                            containerColor = SavingsRateColor,
                            icon = Icons.Filled.Percent,
                            waveVariant = 2,
                        )
                        StatCard(
                            label = "Avg. Daily Spend",
                            value = formatMoney(state.averageDailySpendMinor, state.currency),
                            modifier = Modifier.weight(1f),
                            containerColor = AvgDailySpendColor,
                            icon = Icons.AutoMirrored.Filled.TrendingUp,
                            waveVariant = 3,
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
                            onClick = {
                                if (transaction.isTransfer) {
                                    Toast.makeText(context, "Transfers can't be edited yet — delete and re-create if needed.", Toast.LENGTH_SHORT).show()
                                } else {
                                    onTransactionClick(transaction.id)
                                }
                            },
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
                if (state.upcomingRecurring.isEmpty()) {
                    item {
                        EmptyState(
                            title = "Nothing scheduled",
                            subtitle = "Add a bill, subscription or regular income so it doesn't need to be entered by hand every time.",
                            icon = Icons.Filled.CalendarMonth,
                            actionLabel = "Add recurring payment",
                            onAction = onManageRecurring,
                        )
                    }
                } else {
                    items(state.upcomingRecurring, key = { it.id }) { item ->
                        UpcomingRecurringRow(item = item, onClick = onManageRecurring)
                    }
                    item {
                        Text(
                            text = "Manage recurring payments",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().clickable(onClick = onManageRecurring).padding(8.dp),
                        )
                    }
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

@Composable
private fun UpcomingRecurringRow(item: RecurringTransactionUi, onClick: () -> Unit) {
    val amountColor = if (item.type == TransactionType.INCOME) WalletTheme.extendedColors.income else WalletTheme.extendedColors.expense
    val sign = if (item.type == TransactionType.INCOME) "+" else "-"
    val title = item.payee ?: item.categoryName ?: "Recurring payment"

    LiquidGlassCard(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = GlassShapes.small,
        cornerRadius = 14.dp,
        tint = GlassColors.neutralGlassTint(WalletTheme.extendedColors.transfer),
        lightweight = true,
    ) {
        ListItem(
            leadingContent = {
                Icon(
                    imageVector = if (item.autoPost) Icons.Filled.Autorenew else Icons.Filled.NotificationsActive,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            headlineContent = { Text(title, fontSize = 15.sp) },
            supportingContent = {
                Text("${item.frequency.label()} · Next: ${dateGroupLabel(item.nextDate)}", fontSize = 12.sp)
            },
            trailingContent = {
                Text(
                    text = "$sign ${formatMoney(item.amountMinor, item.currency)}",
                    color = amountColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        )
    }
}

