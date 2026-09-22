package com.example.wallet.feature.budgets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wallet.domain.repository.BudgetRepository
import com.example.wallet.domain.repository.TransactionRepository
import com.example.wallet.domain.repository.TransactionSplitRepository
import com.example.wallet.domain.usecase.budget.CalculateBudgetUsageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
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
    ) { budgets, transactions, splits ->
        val summaries = budgets.map { budget ->
            val categoryIds = budgetRepository.observeBudgetCategories(budget.id).first().map { it.categoryId }.toSet()
            BudgetSummary(budget, calculateBudgetUsage(budget, categoryIds, transactions, splits))
        }
        BudgetsUiState.Loaded(summaries)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = BudgetsUiState.Loading,
    )
}
