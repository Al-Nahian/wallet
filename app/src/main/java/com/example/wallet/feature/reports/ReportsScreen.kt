package com.example.wallet.feature.reports

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.wallet.core.common.formatMoney
import com.example.wallet.core.design.components.BalanceCard
import com.example.wallet.core.design.components.CashFlowCard
import com.example.wallet.core.design.components.CategoryBreakdownRow
import com.example.wallet.core.design.components.DateRangeSelector
import com.example.wallet.core.design.components.StatCard
import com.example.wallet.core.design.components.WalletBottomNavSpace
import com.example.wallet.core.design.glass.GlassStyle
import com.example.wallet.core.design.glass.GlassSurface
import com.example.wallet.domain.usecase.reports.AccountReport
import com.example.wallet.domain.usecase.reports.CategoryTrend
import java.util.Locale

private val ReportsAccentPalette = listOf(
    Color(0xFF42B5E8), Color(0xFF9C6ADE), Color(0xFFFF9F1C), Color(0xFF26A69A),
    Color(0xFFEC407A), Color(0xFF7CB342),
)

/** Same explicit neutral fill CashFlowCard uses, and for the same reason: in light mode
 * [GlassStyle.Thick]'s theme-surface-derived fill sits too close to the page background to read
 * as a card at all. */
private val LightNeutralCardFill = Color(0xFFEDEDF2)
private val DarkNeutralCardFill = Color(0xFF1C1C1E)

/** Solid, full-opacity card colors — mirrors the dashboard's palette so the two screens feel
 * like one cohesive app rather than each inventing its own tinting. */
private val IncomeColor = Color(0xFF16A34A)
private val ExpenseColor = Color(0xFFDC2626)
private val SavingsColor = Color(0xFF16A34A)
private val SavingsRateColor = Color(0xFF0D9488)

@Composable
fun ReportsScreen(modifier: Modifier = Modifier, viewModel: ReportsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val state = uiState) {
        ReportsUiState.Loading -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        is ReportsUiState.Loaded -> {
            LazyColumn(
                modifier = modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 16.dp,
                    bottom = 16.dp + WalletBottomNavSpace,
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item {
                    DateRangeSelector(
                        selected = state.selectedPreset,
                        onSelect = viewModel::onPresetSelected,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                item {
                    // Same CashFlowCard the Home dashboard uses, for the selected report period
                    // instead of "this month" — keeps the two screens visually consistent. No
                    // section header above it: the card carries its own "Cash Flow" title.
                    CashFlowCard(
                        periodLabel = state.selectedPreset.label,
                        incomeMinor = state.totalIncomeMinor,
                        expenseMinor = state.totalExpenseMinor,
                        currency = state.currency,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                item {
                    BalanceCard(
                        label = "Savings",
                        amountMinor = state.savingsMinor,
                        currency = state.currency,
                        containerColor = SavingsColor,
                        contentColor = Color.White,
                        icon = Icons.Filled.Savings,
                    )
                }
                item {
                    StatCard(
                        label = "Savings Rate",
                        value = String.format(Locale.getDefault(), "%.1f%%", state.savingsRatePercent),
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = SavingsRateColor,
                        contentColor = Color.White,
                        icon = Icons.Filled.Percent,
                    )
                }

                item { SectionHeader("Spending by Category") }
                if (state.spendingByCategory.isEmpty()) {
                    item { EmptyRowText("No expenses in this period.") }
                } else {
                    itemsIndexed(state.spendingByCategory, key = { i, s -> "spend-${s.categoryId ?: "uncategorized"}-$i" }) { index, spend ->
                        CategoryBreakdownRow(
                            spend = spend,
                            currency = state.currency,
                            color = ReportsAccentPalette[index % ReportsAccentPalette.size],
                        )
                    }
                }

                item { SectionHeader("Income by Source") }
                if (state.incomeByCategory.isEmpty()) {
                    item { EmptyRowText("No income in this period.") }
                } else {
                    itemsIndexed(state.incomeByCategory, key = { i, s -> "income-${s.categoryId ?: "uncategorized"}-$i" }) { index, spend ->
                        CategoryBreakdownRow(
                            spend = spend,
                            currency = state.currency,
                            color = ReportsAccentPalette[index % ReportsAccentPalette.size],
                        )
                    }
                }

                item { SectionHeader("Accounts") }
                if (state.accountReports.isEmpty()) {
                    item { EmptyRowText("No accounts yet.") }
                } else {
                    items(state.accountReports, key = { it.accountId }) { report ->
                        AccountReportCard(report)
                    }
                }

                item { SectionHeader("Category Trend") }
                if (state.categoryTrends.isEmpty()) {
                    item { EmptyRowText("Not enough history yet to compare trends.") }
                } else {
                    items(state.categoryTrends, key = { it.categoryId ?: "uncategorized" }) { trend ->
                        CategoryTrendCard(trend, state.currency)
                    }
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
private fun EmptyRowText(text: String) {
    Text(text = text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
private fun AccountReportCard(report: AccountReport) {
    val fill = if (isSystemInDarkTheme()) DarkNeutralCardFill else LightNeutralCardFill
    GlassSurface(
        modifier = Modifier.fillMaxWidth(),
        style = GlassStyle.Thick,
        fill = fill.copy(alpha = 0.85f),
        elevation = 0.dp,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = report.accountName, style = MaterialTheme.typography.labelLarge)
            Text(
                text = formatMoney(report.currentBalanceMinor, report.currency),
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                Text(
                    text = "In ${formatMoney(report.inflowMinor, report.currency)}",
                    fontSize = 13.sp,
                    color = IncomeColor,
                )
                Text(
                    text = "Out ${formatMoney(report.outflowMinor, report.currency)}",
                    fontSize = 13.sp,
                    color = ExpenseColor,
                )
            }
        }
    }
}

@Composable
private fun CategoryTrendCard(trend: CategoryTrend, currency: String) {
    val fill = if (isSystemInDarkTheme()) DarkNeutralCardFill else LightNeutralCardFill
    GlassSurface(
        modifier = Modifier.fillMaxWidth(),
        style = GlassStyle.Thick,
        fill = fill.copy(alpha = 0.85f),
        elevation = 0.dp,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = trend.categoryName, style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                TrendStat("Current", trend.currentMonthMinor, currency)
                TrendStat("Previous", trend.previousMonthMinor, currency)
                TrendStat("3mo avg", trend.avg3MonthMinor, currency)
                TrendStat("6mo avg", trend.avg6MonthMinor, currency)
            }
        }
    }
}

@Composable
private fun TrendStat(label: String, amountMinor: Long, currency: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = formatMoney(amountMinor, currency), fontSize = 13.sp)
    }
}
