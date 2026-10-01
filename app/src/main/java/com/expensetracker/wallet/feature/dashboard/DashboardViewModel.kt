package com.expensetracker.wallet.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expensetracker.wallet.core.common.currentDayOfMonth
import com.expensetracker.wallet.core.common.monthRange
import com.expensetracker.wallet.core.common.startOfCurrentMonthMillis
import com.expensetracker.wallet.domain.model.AccountType
import com.expensetracker.wallet.domain.repository.AccountRepository
import com.expensetracker.wallet.domain.repository.BudgetRepository
import com.expensetracker.wallet.domain.repository.CategoryRepository
import com.expensetracker.wallet.domain.repository.RecurringTransactionRepository
import com.expensetracker.wallet.domain.repository.TransactionRepository
import com.expensetracker.wallet.domain.repository.TransactionSplitRepository
import com.expensetracker.wallet.domain.usecase.account.CalculateBalanceUseCase
import com.expensetracker.wallet.domain.usecase.budget.CalculateBudgetUsageUseCase
import com.expensetracker.wallet.domain.usecase.dashboard.CategorySpend
import com.expensetracker.wallet.domain.usecase.dashboard.GetAverageDailySpendUseCase
import com.expensetracker.wallet.domain.usecase.dashboard.GetCategorySpendUseCase
import com.expensetracker.wallet.domain.usecase.dashboard.GetMonthlyExpensesUseCase
import com.expensetracker.wallet.domain.usecase.dashboard.GetMonthlyIncomeUseCase
import com.expensetracker.wallet.domain.usecase.dashboard.GetSavingsRateUseCase
import com.expensetracker.wallet.domain.usecase.dashboard.GetSavingsUseCase
import com.expensetracker.wallet.domain.usecase.dashboard.GetTotalBalanceUseCase
import com.expensetracker.wallet.feature.budgets.BudgetSummary
import com.expensetracker.wallet.feature.recurring.RecurringTransactionUi
import com.expensetracker.wallet.feature.transactions.TransactionUi
import com.expensetracker.wallet.feature.transactions.toTransactionUi
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class AccountBalanceUi(
    val id: String,
    val name: String,
    val type: AccountType,
    val balanceMinor: Long,
    val currency: String,
)

sealed interface DashboardUiState {
    data object Loading : DashboardUiState
    data class Loaded(
        val totalBalanceMinor: Long,
        val currency: String,
        val monthlyIncomeMinor: Long,
        val monthlyExpenseMinor: Long,
        val previousMonthNetMinor: Long,
        val savingsMinor: Long,
        val savingsRatePercent: Double,
        val averageDailySpendMinor: Long,
        val accountBalances: List<AccountBalanceUi>,
        val categorySpend: List<CategorySpend>,
        val recentTransactions: List<TransactionUi>,
        val activeBudgets: List<BudgetSummary>,
        val upcomingRecurring: List<RecurringTransactionUi>,
    ) : DashboardUiState
}

private const val RECENT_TRANSACTIONS_LIMIT = 5
private const val UPCOMING_RECURRING_LIMIT = 3

/** Kotlin's `combine` only has typed overloads up to 5 flows — this holds the first 5 so a 6th
 * (recurring rules) can be folded in via a second, 2-arg `combine` below. */
private data class DashboardBaseData(
    val accounts: List<com.expensetracker.wallet.domain.model.Account>,
    val transactions: List<com.expensetracker.wallet.domain.model.Transaction>,
    val categories: List<com.expensetracker.wallet.domain.model.Category>,
    val splits: List<com.expensetracker.wallet.domain.model.TransactionSplit>,
    val budgets: List<com.expensetracker.wallet.domain.model.Budget>,
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    accountRepository: AccountRepository,
    transactionRepository: TransactionRepository,
    categoryRepository: CategoryRepository,
    transactionSplitRepository: TransactionSplitRepository,
    private val budgetRepository: BudgetRepository,
    recurringTransactionRepository: RecurringTransactionRepository,
    private val getTotalBalance: GetTotalBalanceUseCase,
    private val calculateBalance: CalculateBalanceUseCase,
    private val getMonthlyIncome: GetMonthlyIncomeUseCase,
    private val getMonthlyExpenses: GetMonthlyExpensesUseCase,
    private val getSavings: GetSavingsUseCase,
    private val getSavingsRate: GetSavingsRateUseCase,
    private val getAverageDailySpend: GetAverageDailySpendUseCase,
    private val getCategorySpend: GetCategorySpendUseCase,
    private val calculateBudgetUsage: CalculateBudgetUsageUseCase,
) : ViewModel() {

    // Recomputes on any account/transaction/category/split/budget/recurring-rule change — a
    // dashboard is a summary of everything else in the app, so it needs to react to all six
    // (plan.md §20's "no manual refresh" requirement).
    private val baseData = combine(
        accountRepository.observeActiveAccounts(),
        transactionRepository.observeTransactions(),
        categoryRepository.observeCategories(),
        transactionSplitRepository.observeAllSplits(),
        budgetRepository.observeBudgets(),
    ) { accounts, transactions, categories, splits, budgets ->
        DashboardBaseData(accounts, transactions, categories, splits, budgets)
    }

    val uiState: StateFlow<DashboardUiState> = combine(
        baseData,
        recurringTransactionRepository.observeActive(),
        budgetRepository.observeAllBudgetCategories(),
    ) { (accounts, transactions, categories, splits, budgets), recurringRules, allBudgetCategories ->
        val monthStart = startOfCurrentMonthMillis()
        val now = System.currentTimeMillis()

        val totalBalance = getTotalBalance(accounts)
        val accountBalances = accounts.map { account ->
            AccountBalanceUi(
                id = account.id,
                name = account.name,
                type = account.type,
                balanceMinor = calculateBalance(account),
                currency = account.currency,
            )
        }
        val monthlyIncome = getMonthlyIncome(monthStart, now)
        val monthlyExpense = getMonthlyExpenses(monthStart, now)
        val previousMonth = monthRange(1, now)
        val previousMonthIncome = getMonthlyIncome(previousMonth.startInclusive, previousMonth.endInclusive)
        val previousMonthExpense = getMonthlyExpenses(previousMonth.startInclusive, previousMonth.endInclusive)
        val previousMonthNet = previousMonthIncome - previousMonthExpense
        val savings = getSavings(monthlyIncome, monthlyExpense)
        val savingsRate = getSavingsRate(savings, monthlyIncome)
        val averageDailySpend = getAverageDailySpend(monthlyExpense, currentDayOfMonth(now))

        val categoriesById = categories.associateBy { it.id }
        val monthTransactions = transactions.filter { it.date in monthStart..now }
        val categorySpend = getCategorySpend(monthTransactions, splits, categoriesById)

        val accountsById = accounts.associateBy { it.id }
        val splitsByTransaction = splits.groupBy { it.transactionId }
        val transferLegsByTransferId = transactions.filter { it.transferId != null }.groupBy { it.transferId }
        val recentTransactions = transactions
            .sortedByDescending { it.date }
            .take(RECENT_TRANSACTIONS_LIMIT)
            .map { tx -> tx.toTransactionUi(accountsById, categoriesById, splitsByTransaction, transferLegsByTransferId) }

        val budgetCategoriesByBudget = allBudgetCategories.groupBy { it.budgetId }
        val activeBudgets = budgets
            .filter { now in it.startDate..it.endDate }
            .map { budget ->
                val categoryIds = budgetCategoriesByBudget[budget.id].orEmpty().map { it.categoryId }.toSet()
                BudgetSummary(budget, calculateBudgetUsage(budget, categoryIds, transactions, splits))
            }

        val upcomingRecurring = recurringRules
            .sortedBy { it.nextDate }
            .take(UPCOMING_RECURRING_LIMIT)
            .map { rule ->
                RecurringTransactionUi(
                    id = rule.id,
                    accountName = accountsById[rule.accountId]?.name ?: "Unknown account",
                    categoryName = rule.categoryId?.let { categoriesById[it]?.name },
                    amountMinor = rule.amountMinor,
                    currency = rule.currency,
                    type = rule.type,
                    frequency = rule.frequency,
                    nextDate = rule.nextDate,
                    payee = rule.payee,
                    autoPost = rule.autoPost,
                )
            }

        DashboardUiState.Loaded(
            totalBalanceMinor = totalBalance,
            currency = accounts.firstOrNull()?.currency ?: "BDT",
            monthlyIncomeMinor = monthlyIncome,
            monthlyExpenseMinor = monthlyExpense,
            previousMonthNetMinor = previousMonthNet,
            savingsMinor = savings,
            savingsRatePercent = savingsRate,
            averageDailySpendMinor = averageDailySpend,
            accountBalances = accountBalances,
            categorySpend = categorySpend,
            recentTransactions = recentTransactions,
            activeBudgets = activeBudgets,
            upcomingRecurring = upcomingRecurring,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DashboardUiState.Loading,
    )
}
