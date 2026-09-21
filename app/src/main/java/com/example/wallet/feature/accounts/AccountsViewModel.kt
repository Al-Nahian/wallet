package com.example.wallet.feature.accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wallet.domain.repository.AccountRepository
import com.example.wallet.domain.usecase.account.CalculateBalanceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

sealed interface AccountsUiState {
    data object Loading : AccountsUiState
    data class Loaded(val accounts: List<AccountUi>) : AccountsUiState
}

@HiltViewModel
class AccountsViewModel @Inject constructor(
    accountRepository: AccountRepository,
    calculateBalance: CalculateBalanceUseCase,
) : ViewModel() {

    val uiState: StateFlow<AccountsUiState> = accountRepository.observeActiveAccounts()
        .map { accounts ->
            AccountsUiState.Loaded(
                accounts.map { account ->
                    AccountUi(
                        id = account.id,
                        name = account.name,
                        type = account.type,
                        currency = account.currency,
                        balanceMinor = calculateBalance(account),
                    )
                },
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AccountsUiState.Loading,
        )
}
