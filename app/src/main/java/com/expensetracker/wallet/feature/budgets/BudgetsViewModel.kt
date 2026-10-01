package com.expensetracker.wallet.feature.budgets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expensetracker.wallet.domain.repository.BudgetRepository
import com.expensetracker.wallet.domain.repository.TransactionRepository
import com.expensetracker.wallet.domain.repository.TransactionSplitRepository
import com.expensetracker.wallet.domain.usecase.budget.CalculateBudgetUsageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

sealed interface BudgetsUiState {
    data object Loading : BudgetsUiState
    data class Loaded(val budgets: List<BudgetSummary>) : BudgetsUiState
}

@HiltViewModel
class BudgetsViewModel @Inject constructor(
    private val budgetRepository: BudgetRepository,
    transactionRepository: TransactionRepository,
    transactionSplitRepository: TransactionSplitRepository,
    private val calculateBudgetUsage: CalculateBudgetUsageUseCase,
) : ViewModel() {

    val uiState: StateFlow<BudgetsUiState> = combine(
        budgetRepository.observeBudgets(),
        transactionRepository.observeTransactions(),
        transactionSplitRepository.observeAllSplits(),
        budgetRepository.observeAllBudgetCategories(),
    ) { budgets, transactions, splits, allBudgetCategories ->
        val budgetCategoriesByBudget = allBudgetCategories.groupBy { it.budgetId }
        val summaries = budgets.map { budget ->
            val categoryIds = budgetCategoriesByBudget[budget.id].orEmpty().map { it.categoryId }.toSet()
            BudgetSummary(budget, calculateBudgetUsage(budget, categoryIds, transactions, splits))
        }
        BudgetsUiState.Loaded(summaries)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = BudgetsUiState.Loading,
    )
}
