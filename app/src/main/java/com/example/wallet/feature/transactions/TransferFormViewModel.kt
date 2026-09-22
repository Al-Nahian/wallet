package com.example.wallet.feature.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wallet.core.design.components.SelectorOption
import com.example.wallet.domain.repository.AccountRepository
import com.example.wallet.domain.usecase.transaction.CreateTransferUseCase
import com.example.wallet.domain.usecase.transaction.TransactionValidationException
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TransferFormState(
    val fromAccountId: String? = null,
    val toAccountId: String? = null,
    val amountInput: String = "",
    val note: String = "",
    val date: Long = System.currentTimeMillis(),
    val accountOptions: List<SelectorOption> = emptyList(),
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val saved: Boolean = false,
)

/** plan.md §22 Transfer flow: From Account -> To Account -> Amount -> Date -> Note. Deliberately
 * a separate form/ViewModel from [TransactionFormViewModel] — the field set genuinely differs
 * (two accounts, no category, no payee) rather than overloading the expense/income form. */
@HiltViewModel
class TransferFormViewModel @Inject constructor(
    accountRepository: AccountRepository,
    private val createTransferUseCase: CreateTransferUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransferFormState())
    val uiState: StateFlow<TransferFormState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            accountRepository.observeActiveAccounts().collect { accounts ->
                _uiState.update { it.copy(accountOptions = accounts.map { a -> SelectorOption(a.id, a.name) }) }
            }
        }
    }

    fun onFromAccountChange(id: String) = _uiState.update {
        it.copy(
            fromAccountId = id,
            toAccountId = it.toAccountId.takeIf { to -> to != id },
            errorMessage = null,
        )
    }

    fun onToAccountChange(id: String) = _uiState.update { it.copy(toAccountId = id, errorMessage = null) }
    fun onAmountChange(value: String) = _uiState.update { it.copy(amountInput = value, errorMessage = null) }
    fun onNoteChange(value: String) = _uiState.update { it.copy(note = value) }
    fun onDateChange(value: Long) = _uiState.update { it.copy(date = value) }

    fun save() {
        val state = _uiState.value
        if (state.isSaving) return

        val fromAccountId = state.fromAccountId
        val toAccountId = state.toAccountId
        if (fromAccountId == null || toAccountId == null) {
            _uiState.update { it.copy(errorMessage = "Please select both accounts.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }

            val result = createTransferUseCase(
                fromAccountId = fromAccountId,
                toAccountId = toAccountId,
                amountInput = state.amountInput,
                note = state.note,
                date = state.date,
            )

            result.fold(
                onSuccess = { _uiState.update { it.copy(isSaving = false, saved = true) } },
                onFailure = { error ->
                    val message = (error as? TransactionValidationException)?.error?.userMessage
                        ?: "We couldn't save this transfer. Please try again."
                    _uiState.update { it.copy(isSaving = false, errorMessage = message) }
                },
            )
        }
    }
}
