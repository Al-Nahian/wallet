package com.example.wallet.feature.budgets

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wallet.domain.model.Budget
import com.example.wallet.domain.repository.BudgetRepository
import com.example.wallet.domain.repository.CategoryRepository
import com.example.wallet.domain.repository.TransactionRepository
import com.example.wallet.domain.repository.TransactionSplitRepository
import com.example.wallet.domain.usecase.budget.BudgetUsage
import com.example.wallet.domain.usecase.budget.CalculateBudgetUsageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class BudgetCategoryUsage(
    val categoryId: String,
    val categoryName: String,
    val usage: BudgetUsage,
)

sealed interface BudgetDetailUiState {
    data object Loading : BudgetDetailUiState
    data object NotFound : BudgetDetailUiState
    data class Loaded(
        val budget: Budget,
        val overallUsage: BudgetUsage,
        val categoryUsages: List<BudgetCategoryUsage>,
    ) : BudgetDetailUiState
}

private const val BUDGET_ID_ARG = "budgetId"

/** Per-category usage reuses [CalculateBudgetUsageUseCase] against a copy of the budget whose
 * [Budget.amountMinor] is that category's own limit — the same spend-attribution logic, just
 * scoped to one category and its own limit instead of the budget total. */
@HiltViewModel
class BudgetDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    budgetRepository: BudgetRepository,
    categoryRepository: CategoryRepository,
    transactionRepository: TransactionRepository,
    transactionSplitRepository: TransactionSplitRepository,
    private val calculateBudgetUsage: CalculateBudgetUsageUseCase,
) : ViewModel() {

    private val budgetId: String = checkNotNull(savedStateHandle[BUDGET_ID_ARG])

    val uiState: StateFlow<BudgetDetailUiState> = combine(
        budgetRepository.observeBudget(budgetId),
        budgetRepository.observeBudgetCategories(budgetId),
        categoryRepository.observeCategories(),
        transactionRepository.observeTransactions(),
        transactionSplitRepository.observeAllSplits(),
    ) { budget, budgetCategories, categories, transactions, splits ->
        if (budget == null) return@combine BudgetDetailUiState.NotFound

        val categoriesById = categories.associateBy { it.id }
        val categoryIds = budgetCategories.map { it.categoryId }.toSet()
        val overallUsage = calculateBudgetUsage(budget, categoryIds, transactions, splits)
        val categoryUsages = budgetCategories.map { budgetCategory ->
            val perCategoryBudget = budget.copy(amountMinor = budgetCategory.limitMinor)
            BudgetCategoryUsage(
                categoryId = budgetCategory.categoryId,
                categoryName = categoriesById[budgetCategory.categoryId]?.name ?: "Unknown category",
                usage = calculateBudgetUsage(perCategoryBudget, setOf(budgetCategory.categoryId), transactions, splits),
            )
        }

        BudgetDetailUiState.Loaded(budget, overallUsage, categoryUsages)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = BudgetDetailUiState.Loading,
    )
}
