package com.example.wallet.feature.budgets

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wallet.core.common.minorUnitsToEditableString
import com.example.wallet.core.common.startOfCurrentMonthMillis
import com.example.wallet.domain.model.Budget
import com.example.wallet.domain.model.BudgetPeriod
import com.example.wallet.domain.model.Category
import com.example.wallet.domain.model.CategoryGroup
import com.example.wallet.domain.model.CategoryType
import com.example.wallet.domain.repository.BudgetRepository
import com.example.wallet.domain.repository.CategoryRepository
import com.example.wallet.domain.usecase.budget.BudgetValidationException
import com.example.wallet.domain.usecase.budget.CreateBudgetUseCase
import com.example.wallet.domain.usecase.budget.UpdateBudgetUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Calendar
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BudgetFormState(
    val isEditMode: Boolean = false,
    val isLoading: Boolean = false,
    val name: String = "",
    val period: BudgetPeriod = BudgetPeriod.MONTHLY,
    val startDate: Long = startOfCurrentMonthMillis(),
    val endDate: Long = defaultEndOfMonth(),
    val amountInput: String = "",
    val currency: String = "BDT",
    val expenseGroups: List<CategoryGroup> = emptyList(),
    val expenseCategories: List<Category> = emptyList(),
    val selectedCategoryLimits: Map<String, String> = emptyMap(),
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val saved: Boolean = false,
)

private fun defaultEndOfMonth(): Long = Calendar.getInstance().apply {
    set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
    set(Calendar.HOUR_OF_DAY, 23)
    set(Calendar.MINUTE, 59)
    set(Calendar.SECOND, 59)
    set(Calendar.MILLISECOND, 999)
}.timeInMillis

private const val BUDGET_ID_ARG = "budgetId"

@HiltViewModel
class BudgetFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val budgetRepository: BudgetRepository,
    private val categoryRepository: CategoryRepository,
    private val createBudgetUseCase: CreateBudgetUseCase,
    private val updateBudgetUseCase: UpdateBudgetUseCase,
) : ViewModel() {

    private val budgetId: String? = savedStateHandle[BUDGET_ID_ARG]
    private var existingBudget: Budget? = null

    private val _uiState = MutableStateFlow(
        BudgetFormState(isEditMode = budgetId != null, isLoading = budgetId != null),
    )
    val uiState: StateFlow<BudgetFormState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val groups = categoryRepository.observeGroups().first()
            val categories = categoryRepository.observeCategories().first()
            val expenseGroups = groups.filter { it.type == CategoryType.EXPENSE }
            val expenseGroupIds = expenseGroups.map { it.id }.toSet()
            val expenseCategories = categories.filter { it.groupId in expenseGroupIds }
            _uiState.update { it.copy(expenseGroups = expenseGroups, expenseCategories = expenseCategories) }
        }

        budgetId?.let(::loadExistingBudget)
    }

    private fun loadExistingBudget(id: String) {
        viewModelScope.launch {
            val budget = budgetRepository.observeBudget(id).first()
            if (budget == null) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Budget not found.") }
                return@launch
            }
            existingBudget = budget
            val categoryLimits = budgetRepository.observeBudgetCategories(id).first()
                .associate { it.categoryId to minorUnitsToEditableString(it.limitMinor) }
            _uiState.update {
                it.copy(
                    isLoading = false,
                    name = budget.name,
                    period = budget.period,
                    startDate = budget.startDate,
                    endDate = budget.endDate,
                    amountInput = minorUnitsToEditableString(budget.amountMinor),
                    currency = budget.currency,
                    selectedCategoryLimits = categoryLimits,
                )
            }
        }
    }

    fun onNameChange(value: String) = _uiState.update { it.copy(name = value, errorMessage = null) }
    fun onPeriodChange(value: BudgetPeriod) = _uiState.update { it.copy(period = value) }
    fun onStartDateChange(value: Long) = _uiState.update { it.copy(startDate = value, errorMessage = null) }
    fun onEndDateChange(value: Long) = _uiState.update { it.copy(endDate = value, errorMessage = null) }
    fun onAmountChange(value: String) = _uiState.update { it.copy(amountInput = value, errorMessage = null) }

    fun onToggleCategory(categoryId: String) = _uiState.update { state ->
        val current = state.selectedCategoryLimits
        state.copy(
            selectedCategoryLimits = if (categoryId in current) current - categoryId else current + (categoryId to ""),
        )
    }

    fun onCategoryLimitChange(categoryId: String, value: String) = _uiState.update { state ->
        state.copy(selectedCategoryLimits = state.selectedCategoryLimits + (categoryId to value))
    }

    fun save() {
        val state = _uiState.value
        if (state.isSaving) return

        val categoryLimitInputs = state.selectedCategoryLimits.entries
            .filter { it.value.isNotBlank() }
            .map { it.key to it.value }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }

            val result = if (state.isEditMode && existingBudget != null) {
                updateBudgetUseCase(
                    existing = existingBudget!!,
                    name = state.name,
                    period = state.period,
                    startDate = state.startDate,
                    endDate = state.endDate,
                    amountInput = state.amountInput,
                    categoryLimitInputs = categoryLimitInputs,
                )
            } else {
                createBudgetUseCase(
                    name = state.name,
                    period = state.period,
                    startDate = state.startDate,
                    endDate = state.endDate,
                    amountInput = state.amountInput,
                    currency = state.currency,
                    categoryLimitInputs = categoryLimitInputs,
                )
            }

            result.fold(
                onSuccess = { _uiState.update { it.copy(isSaving = false, saved = true) } },
                onFailure = { error ->
                    val message = (error as? BudgetValidationException)?.error?.userMessage
                        ?: "We couldn't save this budget. Please try again."
                    _uiState.update { it.copy(isSaving = false, errorMessage = message) }
                },
            )
        }
    }
}
