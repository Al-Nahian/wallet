package com.example.wallet.feature.accounts

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wallet.domain.model.AccountType
import com.example.wallet.domain.repository.AccountRepository
import com.example.wallet.domain.repository.InstitutionRepository
import com.example.wallet.domain.usecase.account.AccountValidationException
import com.example.wallet.domain.usecase.account.CreateAccountUseCase
import com.example.wallet.domain.usecase.account.UpdateAccountUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AccountFormState(
    val isEditMode: Boolean = false,
    val isLoading: Boolean = false,
    val name: String = "",
    val type: AccountType = AccountType.BANK,
    val institutionName: String = "",
    val currency: String = "BDT",
    val openingBalanceInput: String = "",
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val saved: Boolean = false,
)

private const val ACCOUNT_ID_ARG = "accountId"

@HiltViewModel
class AccountFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val accountRepository: AccountRepository,
    private val institutionRepository: InstitutionRepository,
    private val createAccountUseCase: CreateAccountUseCase,
    private val updateAccountUseCase: UpdateAccountUseCase,
) : ViewModel() {

    private val accountId: String? = savedStateHandle[ACCOUNT_ID_ARG]

    private val _uiState = MutableStateFlow(
        AccountFormState(isEditMode = accountId != null, isLoading = accountId != null),
    )
    val uiState: StateFlow<AccountFormState> = _uiState.asStateFlow()

    init {
        accountId?.let(::loadExistingAccount)
    }

    private fun loadExistingAccount(id: String) {
        viewModelScope.launch {
            val account = accountRepository.getAccount(id)
            if (account == null) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Account not found.") }
                return@launch
            }
            val institutionName = account.institutionId?.let { institutionRepository.getById(it)?.name }
            _uiState.update {
                it.copy(
                    isLoading = false,
                    name = account.name,
                    type = account.type,
                    institutionName = institutionName.orEmpty(),
                    currency = account.currency,
                )
            }
        }
    }

    fun onNameChange(value: String) = _uiState.update { it.copy(name = value, errorMessage = null) }
    fun onTypeChange(value: AccountType) = _uiState.update { it.copy(type = value) }
    fun onInstitutionChange(value: String) = _uiState.update { it.copy(institutionName = value) }
    fun onCurrencyChange(value: String) = _uiState.update { it.copy(currency = value, errorMessage = null) }
    fun onOpeningBalanceChange(value: String) =
        _uiState.update { it.copy(openingBalanceInput = value, errorMessage = null) }

    fun save() {
        val state = _uiState.value
        if (state.isSaving) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }

            val result = if (state.isEditMode && accountId != null) {
                updateAccountUseCase(
                    accountId = accountId,
                    name = state.name,
                    type = state.type,
                    institutionName = state.institutionName,
                )
            } else {
                createAccountUseCase(
                    name = state.name,
                    type = state.type,
                    institutionName = state.institutionName,
                    currency = state.currency,
                    openingBalanceInput = state.openingBalanceInput,
                )
            }

            result.fold(
                onSuccess = { _uiState.update { it.copy(isSaving = false, saved = true) } },
                onFailure = { error ->
                    val message = (error as? AccountValidationException)?.error?.userMessage
                        ?: "We couldn't save this account. Please try again."
                    _uiState.update { it.copy(isSaving = false, errorMessage = message) }
                },
            )
        }
    }
}
