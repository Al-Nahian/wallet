package com.example.wallet.feature.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wallet.core.common.DateRange
import com.example.wallet.core.common.ReportRangePreset
import com.example.wallet.core.common.dateRangeForPreset
import com.example.wallet.core.common.monthRange
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.wallet.domain.model.Transaction
import com.example.wallet.domain.model.TransactionType
import com.example.wallet.domain.repository.AccountRepository
import com.example.wallet.domain.repository.CategoryRepository
import com.example.wallet.domain.repository.TransactionRepository
import com.example.wallet.domain.repository.TransactionSplitRepository
import com.example.wallet.domain.usecase.dashboard.CategorySpend
import com.example.wallet.domain.usecase.dashboard.GetCategorySpendUseCase
import com.example.wallet.domain.usecase.dashboard.GetSavingsRateUseCase
import com.example.wallet.domain.usecase.dashboard.GetSavingsUseCase
import com.example.wallet.domain.usecase.reports.AccountReport
import com.example.wallet.domain.usecase.reports.CategoryTrend
import com.example.wallet.domain.usecase.reports.GetAccountReportUseCase
import com.example.wallet.domain.usecase.reports.GetCategoryTrendUseCase
import com.example.wallet.domain.usecase.transaction.CalculateCashFlowUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

sealed interface ReportsUiState {
    data object Loading : ReportsUiState
    data class Loaded(
        val selectedPreset: ReportRangePreset,
        val customRange: DateRange?,
        val customRangeLabel: String?,
        val currency: String,
        val totalIncomeMinor: Long,
        val totalExpenseMinor: Long,
        val cashFlowMinor: Long,
        val savingsMinor: Long,
        val savingsRatePercent: Double,
        val spendingByCategory: List<CategorySpend>,
        val incomeByCategory: List<CategorySpend>,
        val accountReports: List<AccountReport>,
        val categoryTrends: List<CategoryTrend>,
    ) : ReportsUiState
}

/**
 * plan.md §24/§25/§65 Milestone 8 — deeper, filterable analysis beyond the dashboard snapshot.
 * Recomputes on any account/transaction/category/split change plus the selected date-range
 * preset (mirrors [com.example.wallet.feature.dashboard.DashboardViewModel]'s combine shape).
 * Income/expense/cash-flow/savings totals and per-account inflow/outflow are DB-aggregated
 * (§54); the category breakdown and trend comparison reuse Phase 8's split-aware in-memory
 * attribution (`GetCategorySpendUseCase`) since that logic doesn't reduce to a single `GROUP BY`
 * without a union across split/non-split rows — a documented tradeoff, not an oversight.
 */
@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,
    categoryRepository: CategoryRepository,
    transactionSplitRepository: TransactionSplitRepository,
    private val getCategorySpend: GetCategorySpendUseCase,
    private val calculateCashFlow: CalculateCashFlowUseCase,
    private val getSavings: GetSavingsUseCase,
    private val getSavingsRate: GetSavingsRateUseCase,
    private val getAccountReport: GetAccountReportUseCase,
    private val getCategoryTrend: GetCategoryTrendUseCase,
) : ViewModel() {

    private val selectedPreset = MutableStateFlow(ReportRangePreset.THIS_MONTH)
    private val customRange = MutableStateFlow<DateRange?>(null)

    // kotlinx.coroutines' combine() only has typed overloads up to 5 flows — folding the two
    // range-selection flows into one Pair first keeps the main combine below at 5 instead of 6.
    private val rangeSelection = combine(selectedPreset, customRange) { preset, custom -> preset to custom }

    val uiState: StateFlow<ReportsUiState> = combine(
        accountRepository.observeActiveAccounts(),
        transactionRepository.observeTransactions(),
        categoryRepository.observeCategories(),
        transactionSplitRepository.observeAllSplits(),
        rangeSelection,
    ) { accounts, transactions, categories, splits, (preset, custom) ->
        val now = System.currentTimeMillis()
        val range = dateRangeForPreset(preset, now, custom)
        val categoriesById = categories.associateBy { it.id }

        val totalIncome = transactionRepository.sumByTypeInRange(TransactionType.INCOME, range.startInclusive, range.endInclusive)
        val totalExpense = transactionRepository.sumByTypeInRange(TransactionType.EXPENSE, range.startInclusive, range.endInclusive)
        val cashFlow = calculateCashFlow(range.startInclusive, range.endInclusive)
        val savings = getSavings(totalIncome, totalExpense)
        val savingsRate = getSavingsRate(savings, totalIncome)

        fun transactionsInRange(startInclusive: Long, endInclusive: Long): List<Transaction> =
            transactions.filter { it.date in startInclusive..endInclusive }

        val rangeTransactions = transactionsInRange(range.startInclusive, range.endInclusive)
        val spendingByCategory = getCategorySpend(rangeTransactions, splits, categoriesById, TransactionType.EXPENSE)
        val incomeByCategory = getCategorySpend(rangeTransactions, splits, categoriesById, TransactionType.INCOME)

        val accountReports = accounts.map { account ->
            getAccountReport(account, range.startInclusive, range.endInclusive)
        }

        fun categorySpendForMonth(monthsAgo: Int): List<CategorySpend> {
            val monthWindow = monthRange(monthsAgo, now)
            return getCategorySpend(
                transactionsInRange(monthWindow.startInclusive, monthWindow.endInclusive),
                splits,
                categoriesById,
                TransactionType.EXPENSE,
            )
        }
        val categoryTrends = getCategoryTrend(
            currentMonth = categorySpendForMonth(0),
            previousMonth = categorySpendForMonth(1),
            last3Months = (0..2).map(::categorySpendForMonth),
            last6Months = (0..5).map(::categorySpendForMonth),
        )

        val customRangeLabel = custom?.let {
            val formatter = SimpleDateFormat("MMM d", Locale.getDefault())
            "${formatter.format(Date(it.startInclusive))} – ${formatter.format(Date(it.endInclusive))}"
        }

        ReportsUiState.Loaded(
            selectedPreset = preset,
            customRange = custom,
            customRangeLabel = customRangeLabel,
            currency = accounts.firstOrNull()?.currency ?: "BDT",
            totalIncomeMinor = totalIncome,
            totalExpenseMinor = totalExpense,
            cashFlowMinor = cashFlow,
            savingsMinor = savings,
            savingsRatePercent = savingsRate,
            spendingByCategory = spendingByCategory,
            incomeByCategory = incomeByCategory,
            accountReports = accountReports,
            categoryTrends = categoryTrends,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ReportsUiState.Loading,
    )

    fun onPresetSelected(preset: ReportRangePreset) {
        selectedPreset.value = preset
    }

    /** From the custom date-range picker — sets the range and switches the selector to
     * [ReportRangePreset.CUSTOM] in one step, so picking a range is itself the selection. */
    fun onCustomRangeSelected(range: DateRange) {
        customRange.value = range
        selectedPreset.value = ReportRangePreset.CUSTOM
    }
}
