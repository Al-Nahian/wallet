package com.example.wallet.feature.transactions

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wallet.core.common.minorUnitsToEditableString
import com.example.wallet.core.common.parseMoneyToMinorUnits
import com.example.wallet.core.design.components.SelectorOption
import com.example.wallet.domain.model.TransactionType
import com.example.wallet.domain.repository.AccountRepository
import com.example.wallet.domain.repository.CategoryRepository
import com.example.wallet.domain.repository.TransactionRepository
import com.example.wallet.domain.repository.TransactionSplitRepository
import com.example.wallet.domain.usecase.transaction.CreateTransactionUseCase
import com.example.wallet.domain.usecase.transaction.SplitInput
import com.example.wallet.domain.usecase.transaction.SplitTransactionUseCase
import com.example.wallet.domain.usecase.transaction.TransactionValidationException
import com.example.wallet.domain.usecase.transaction.UpdateTransactionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** One row in the split editor (plan.md §13). [key] is a stable Compose list key, independent
 * of the eventual persisted split id. */
data class SplitRowState(
    val key: String = UUID.randomUUID().toString(),
    val categoryId: String? = null,
    val amountInput: String = "",
    val note: String = "",
)

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
    val isSplitEnabled: Boolean = false,
    val splitRows: List<SplitRowState> = emptyList(),
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val saved: Boolean = false,
)

private const val TRANSACTION_ID_ARG = "transactionId"

@HiltViewModel
class TransactionFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val transactionRepository: TransactionRepository,
    private val transactionSplitRepository: TransactionSplitRepository,
    accountRepository: AccountRepository,
    categoryRepository: CategoryRepository,
    private val createTransactionUseCase: CreateTransactionUseCase,
    private val updateTransactionUseCase: UpdateTransactionUseCase,
    private val splitTransactionUseCase: SplitTransactionUseCase,
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
            val existingSplits = transactionSplitRepository.observeByTransaction(id).first()
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
                    isSplitEnabled = existingSplits.isNotEmpty(),
                    splitRows = existingSplits.map { split ->
                        SplitRowState(
                            categoryId = split.categoryId,
                            amountInput = minorUnitsToEditableString(split.amountMinor),
                            note = split.note.orEmpty(),
                        )
                    },
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

    fun onToggleSplit(enabled: Boolean) = _uiState.update {
        it.copy(
            isSplitEnabled = enabled,
            splitRows = if (enabled && it.splitRows.size < 2) listOf(SplitRowState(), SplitRowState()) else it.splitRows,
            errorMessage = null,
        )
    }

    fun addSplitRow() = _uiState.update { it.copy(splitRows = it.splitRows + SplitRowState()) }

    fun removeSplitRow(key: String) = _uiState.update { it.copy(splitRows = it.splitRows.filterNot { row -> row.key == key }) }

    fun onSplitCategoryChange(key: String, categoryId: String) = _uiState.update {
        it.copy(splitRows = it.splitRows.map { row -> if (row.key == key) row.copy(categoryId = categoryId) else row })
    }

    fun onSplitAmountChange(key: String, value: String) = _uiState.update {
        it.copy(splitRows = it.splitRows.map { row -> if (row.key == key) row.copy(amountInput = value) else row })
    }

    fun onSplitNoteChange(key: String, value: String) = _uiState.update {
        it.copy(splitRows = it.splitRows.map { row -> if (row.key == key) row.copy(note = value) else row })
    }

    fun save() {
        val state = _uiState.value
        if (state.isSaving) return

        val accountId = state.accountId
        if (accountId == null) {
            _uiState.update { it.copy(errorMessage = "Please select an account.") }
            return
        }

        var splitInputs: List<SplitInput>? = null
        if (state.isSplitEnabled) {
            val targetMinor = parseMoneyToMinorUnits(state.amountInput)
            val parsedRows = state.splitRows.map { row ->
                Triple(row.categoryId, parseMoneyToMinorUnits(row.amountInput), row.note.trim().takeIf { it.isNotEmpty() })
            }
            val hasInvalidRow = parsedRows.any { (categoryId, amount, _) -> categoryId == null || amount == null || amount <= 0 }
            if (state.splitRows.size < 2 || hasInvalidRow) {
                _uiState.update { it.copy(errorMessage = "Add at least two splits, each with a category and an amount.") }
                return
            }
            val sum = parsedRows.sumOf { it.second!! }
            if (targetMinor == null || sum != targetMinor) {
                _uiState.update { it.copy(errorMessage = "Split amounts must add up to the transaction total.") }
                return
            }
            splitInputs = parsedRows.map { (categoryId, amount, note) -> SplitInput(categoryId!!, amount!!, note) }
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }

            // A split transaction's category is "Split", not one arbitrary category (plan.md §13).
            val categoryIdForSave = if (state.isSplitEnabled) null else state.categoryId

            val result = if (state.isEditMode && transactionId != null) {
                updateTransactionUseCase(
                    transactionId = transactionId,
                    type = state.type,
                    accountId = accountId,
                    amountInput = state.amountInput,
                    categoryId = categoryIdForSave,
                    payee = state.payee,
                    note = state.note,
                    date = state.date,
                )
            } else {
                createTransactionUseCase(
                    type = state.type,
                    accountId = accountId,
                    amountInput = state.amountInput,
                    categoryId = categoryIdForSave,
                    payee = state.payee,
                    note = state.note,
                    date = state.date,
                )
            }

            result.fold(
                onSuccess = { transaction ->
                    val splits = splitInputs
                    if (splits == null) {
                        _uiState.update { it.copy(isSaving = false, saved = true) }
                    } else {
                        splitTransactionUseCase(transaction.id, splits).fold(
                            onSuccess = { _uiState.update { it.copy(isSaving = false, saved = true) } },
                            onFailure = { error -> _uiState.update { it.copy(isSaving = false, errorMessage = errorMessageFor(error)) } },
                        )
                    }
                },
                onFailure = { error -> _uiState.update { it.copy(isSaving = false, errorMessage = errorMessageFor(error)) } },
            )
        }
    }

    private fun errorMessageFor(error: Throwable): String =
        (error as? TransactionValidationException)?.error?.userMessage
            ?: "We couldn't save this transaction. Please try again."
}
