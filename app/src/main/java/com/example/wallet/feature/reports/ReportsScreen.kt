package com.example.wallet.feature.reports

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.example.wallet.core.design.components.CategoryBreakdownRow
import com.example.wallet.core.design.components.DateRangeSelector
import com.example.wallet.core.design.components.StatCard
import com.example.wallet.core.design.glass.GlassStyle
import com.example.wallet.core.design.glass.GlassSurface
import com.example.wallet.domain.usecase.reports.AccountReport
import com.example.wallet.domain.usecase.reports.CategoryTrend
import java.util.Locale

private val ReportsAccentPalette = listOf(
    Color(0xFF42B5E8), Color(0xFF9C6ADE), Color(0xFFFF9F1C), Color(0xFF26A69A),
    Color(0xFFEC407A), Color(0xFF7CB342),
)

/** Solid, full-opacity card colors — mirrors the dashboard's palette so the two screens feel
 * like one cohesive app rather than each inventing its own tinting. */
private val IncomeColor = Color(0xFF16A34A)
private val ExpenseColor = Color(0xFFDC2626)
private val NetCashFlowColor = Color(0xFF5B4FE0)
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
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item {
                    DateRangeSelector(
                        selected = state.selectedPreset,
                        onSelect = viewModel::onPresetSelected,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                item { SectionHeader("Cash Flow") }
                item {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
                    ) {
                        BalanceCard(
                            label = "Income",
                            amountMinor = state.totalIncomeMinor,
                            currency = state.currency,
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            containerColor = IncomeColor,
                            contentColor = Color.White,
                        )
                        BalanceCard(
                            label = "Expense",
                            amountMinor = state.totalExpenseMinor,
                            currency = state.currency,
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            containerColor = ExpenseColor,
                            contentColor = Color.White,
                        )
                    }
                }
                item {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
                    ) {
                        BalanceCard(
                            label = "Net Cash Flow",
                            amountMinor = state.cashFlowMinor,
                            currency = state.currency,
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            containerColor = NetCashFlowColor,
                            contentColor = Color.White,
                        )
                        BalanceCard(
                            label = "Savings",
                            amountMinor = state.savingsMinor,
                            currency = state.currency,
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            containerColor = SavingsColor,
                            contentColor = Color.White,
                        )
                    }
                }
                item {
                    StatCard(
                        label = "Savings Rate",
                        value = String.format(Locale.getDefault(), "%.1f%%", state.savingsRatePercent),
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = SavingsRateColor,
                        contentColor = Color.White,
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
    GlassSurface(modifier = Modifier.fillMaxWidth(), style = GlassStyle.Thick, elevation = 0.dp) {
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
    GlassSurface(modifier = Modifier.fillMaxWidth(), style = GlassStyle.Thick, elevation = 0.dp) {
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
