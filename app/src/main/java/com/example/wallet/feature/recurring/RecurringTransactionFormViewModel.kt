package com.example.wallet.feature.recurring

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wallet.core.common.minorUnitsToEditableString
import com.example.wallet.core.design.components.SelectorOption
import com.example.wallet.domain.model.Category
import com.example.wallet.domain.model.CategoryGroup
import com.example.wallet.domain.model.RecurringFrequency
import com.example.wallet.domain.model.RecurringTransaction
import com.example.wallet.domain.model.TransactionType
import com.example.wallet.domain.repository.AccountRepository
import com.example.wallet.domain.repository.CategoryRepository
import com.example.wallet.domain.repository.RecurringTransactionRepository
import com.example.wallet.domain.usecase.recurring.CreateRecurringTransactionUseCase
import com.example.wallet.domain.usecase.recurring.RecurringTransactionValidationException
import com.example.wallet.domain.usecase.recurring.UpdateRecurringTransactionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RecurringTransactionFormState(
    val isEditMode: Boolean = false,
    val isLoading: Boolean = false,
    val type: TransactionType = TransactionType.EXPENSE,
    val accountId: String? = null,
    val categoryId: String? = null,
    val amountInput: String = "",
    val frequency: RecurringFrequency = RecurringFrequency.MONTHLY,
    val nextDate: Long = System.currentTimeMillis(),
    val hasEndDate: Boolean = false,
    val endDate: Long = System.currentTimeMillis(),
    val payee: String = "",
    val note: String = "",
    /** true: post the transaction automatically the moment it's due. false: just remind — the
     * user records it manually from the list. */
    val autoPost: Boolean = true,
    val accountOptions: List<SelectorOption> = emptyList(),
    val categoryGroups: List<CategoryGroup> = emptyList(),
    val categories: List<Category> = emptyList(),
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val saved: Boolean = false,
)

private const val RECURRING_ID_ARG = "recurringId"

@HiltViewModel
class RecurringTransactionFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val recurringTransactionRepository: RecurringTransactionRepository,
    accountRepository: AccountRepository,
    categoryRepository: CategoryRepository,
    private val createRecurringTransactionUseCase: CreateRecurringTransactionUseCase,
    private val updateRecurringTransactionUseCase: UpdateRecurringTransactionUseCase,
) : ViewModel() {

    private val recurringId: String? = savedStateHandle[RECURRING_ID_ARG]
    private var existing: RecurringTransaction? = null

    private val _uiState = MutableStateFlow(
        RecurringTransactionFormState(isEditMode = recurringId != null, isLoading = recurringId != null),
    )
    val uiState: StateFlow<RecurringTransactionFormState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                accountRepository.observeAllAccounts(),
                categoryRepository.observeGroups(),
                categoryRepository.observeCategories(),
            ) { accounts, groups, categories ->
                Triple(accounts.map { SelectorOption(it.id, it.name) }, groups, categories)
            }.collect { (accountOptions, groups, categories) ->
                _uiState.update { it.copy(accountOptions = accountOptions, categoryGroups = groups, categories = categories) }
            }
        }

        recurringId?.let(::loadExisting)
    }

    private fun loadExisting(id: String) {
        viewModelScope.launch {
            val rule = recurringTransactionRepository.getById(id)
            if (rule == null) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Recurring payment not found.") }
                return@launch
            }
            existing = rule
            _uiState.update {
                it.copy(
                    isLoading = false,
                    type = rule.type,
                    accountId = rule.accountId,
                    categoryId = rule.categoryId,
                    amountInput = minorUnitsToEditableString(rule.amountMinor),
                    frequency = rule.frequency,
                    nextDate = rule.nextDate,
                    hasEndDate = rule.endDate != null,
                    endDate = rule.endDate ?: System.currentTimeMillis(),
                    payee = rule.payee.orEmpty(),
                    note = rule.note.orEmpty(),
                    autoPost = rule.autoPost,
                )
            }
        }
    }

    fun onTypeChange(value: TransactionType) = _uiState.update { it.copy(type = value, errorMessage = null) }
    fun onAccountChange(value: String) = _uiState.update { it.copy(accountId = value, errorMessage = null) }
    fun onCategoryChange(value: String?) = _uiState.update { it.copy(categoryId = value) }
    fun onAmountChange(value: String) = _uiState.update { it.copy(amountInput = value, errorMessage = null) }
    fun onFrequencyChange(value: RecurringFrequency) = _uiState.update { it.copy(frequency = value) }
    fun onNextDateChange(value: Long) = _uiState.update { it.copy(nextDate = value, errorMessage = null) }
    fun onHasEndDateChange(value: Boolean) = _uiState.update { it.copy(hasEndDate = value, errorMessage = null) }
    fun onEndDateChange(value: Long) = _uiState.update { it.copy(endDate = value, errorMessage = null) }
    fun onPayeeChange(value: String) = _uiState.update { it.copy(payee = value) }
    fun onNoteChange(value: String) = _uiState.update { it.copy(note = value) }
    fun onAutoPostChange(value: Boolean) = _uiState.update { it.copy(autoPost = value) }

    fun save() {
        val state = _uiState.value
        if (state.isSaving) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }

            val endDate = state.endDate.takeIf { state.hasEndDate }
            val result = if (state.isEditMode && existing != null) {
                updateRecurringTransactionUseCase(
                    existing = existing!!,
                    accountId = state.accountId,
                    categoryId = state.categoryId,
                    amountInput = state.amountInput,
                    frequency = state.frequency,
                    nextDate = state.nextDate,
                    endDate = endDate,
                    type = state.type,
                    payee = state.payee,
                    note = state.note,
                    autoPost = state.autoPost,
                    isActive = true,
                )
            } else {
                createRecurringTransactionUseCase(
                    accountId = state.accountId,
                    categoryId = state.categoryId,
                    amountInput = state.amountInput,
                    frequency = state.frequency,
                    nextDate = state.nextDate,
                    endDate = endDate,
                    type = state.type,
                    payee = state.payee,
                    note = state.note,
                    autoPost = state.autoPost,
                )
            }

            result.fold(
                onSuccess = { _uiState.update { it.copy(isSaving = false, saved = true) } },
                onFailure = { error ->
                    val message = (error as? RecurringTransactionValidationException)?.error?.userMessage
                        ?: "We couldn't save this recurring payment. Please try again."
                    _uiState.update { it.copy(isSaving = false, errorMessage = message) }
                },
            )
        }
    }
}
