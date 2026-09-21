package com.example.wallet.feature.transactions

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wallet.core.common.minorUnitsToEditableString
import com.example.wallet.core.design.components.SelectorOption
import com.example.wallet.domain.model.TransactionType
import com.example.wallet.domain.repository.AccountRepository
import com.example.wallet.domain.repository.CategoryRepository
import com.example.wallet.domain.repository.TransactionRepository
import com.example.wallet.domain.usecase.transaction.CreateTransactionUseCase
import com.example.wallet.domain.usecase.transaction.TransactionValidationException
import com.example.wallet.domain.usecase.transaction.UpdateTransactionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TransactionFormState(
    val isEditMode: Boolean = false,
    val isLoading: Boolean = false,
    val type: TransactionType = TransactionType.EXPENSE,
    val accountId: String? = null,
    val amountInput: String = "",
    val categoryId: String? = null,
    val payee: String = "",
    val note: String = "",
    val date: Long = System.currentTimeMillis(),
    val accountOptions: List<SelectorOption> = emptyList(),
    val categoryOptions: List<SelectorOption> = emptyList(),
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val saved: Boolean = false,
)

private const val TRANSACTION_ID_ARG = "transactionId"

@HiltViewModel
class TransactionFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val transactionRepository: TransactionRepository,
    accountRepository: AccountRepository,
    categoryRepository: CategoryRepository,
    private val createTransactionUseCase: CreateTransactionUseCase,
    private val updateTransactionUseCase: UpdateTransactionUseCase,
) : ViewModel() {

    private val transactionId: String? = savedStateHandle[TRANSACTION_ID_ARG]

    private val _uiState = MutableStateFlow(
        TransactionFormState(isEditMode = transactionId != null, isLoading = transactionId != null),
    )
    val uiState: StateFlow<TransactionFormState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                accountRepository.observeActiveAccounts(),
                categoryRepository.observeCategories(),
            ) { accounts, categories ->
                accounts.map { SelectorOption(it.id, it.name) } to
                    categories.map { SelectorOption(it.id, it.name) }
            }.collect { (accountOptions, categoryOptions) ->
                _uiState.update { it.copy(accountOptions = accountOptions, categoryOptions = categoryOptions) }
            }
        }

        transactionId?.let(::loadExistingTransaction)
    }

    private fun loadExistingTransaction(id: String) {
        viewModelScope.launch {
            val transaction = transactionRepository.getTransaction(id)
            if (transaction == null) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Transaction not found.") }
                return@launch
            }
            _uiState.update {
                it.copy(
                    isLoading = false,
                    type = transaction.type,
                    accountId = transaction.accountId,
                    amountInput = minorUnitsToEditableString(transaction.amountMinor),
                    categoryId = transaction.categoryId,
                    payee = transaction.payee.orEmpty(),
                    note = transaction.note.orEmpty(),
                    date = transaction.date,
                )
            }
        }
    }

    fun onTypeChange(type: TransactionType) = _uiState.update { it.copy(type = type) }
    fun onAccountChange(id: String) = _uiState.update { it.copy(accountId = id, errorMessage = null) }
    fun onAmountChange(value: String) = _uiState.update { it.copy(amountInput = value, errorMessage = null) }
    fun onCategoryChange(id: String?) = _uiState.update { it.copy(categoryId = id) }
    fun onPayeeChange(value: String) = _uiState.update { it.copy(payee = value) }
    fun onNoteChange(value: String) = _uiState.update { it.copy(note = value) }
    fun onDateChange(value: Long) = _uiState.update { it.copy(date = value) }

    fun save() {
        val state = _uiState.value
        if (state.isSaving) return

        val accountId = state.accountId
        if (accountId == null) {
            _uiState.update { it.copy(errorMessage = "Please select an account.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }

            val result = if (state.isEditMode && transactionId != null) {
                updateTransactionUseCase(
                    transactionId = transactionId,
                    type = state.type,
                    accountId = accountId,
                    amountInput = state.amountInput,
                    categoryId = state.categoryId,
                    payee = state.payee,
                    note = state.note,
                    date = state.date,
                )
            } else {
                createTransactionUseCase(
                    type = state.type,
                    accountId = accountId,
                    amountInput = state.amountInput,
                    categoryId = state.categoryId,
                    payee = state.payee,
                    note = state.note,
                    date = state.date,
                )
            }

            result.fold(
                onSuccess = { _uiState.update { it.copy(isSaving = false, saved = true) } },
                onFailure = { error ->
                    val message = (error as? TransactionValidationException)?.error?.userMessage
                        ?: "We couldn't save this transaction. Please try again."
                    _uiState.update { it.copy(isSaving = false, errorMessage = message) }
                },
            )
        }
    }
}
