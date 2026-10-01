package com.expensetracker.wallet.feature.accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expensetracker.wallet.domain.repository.AccountRepository
import com.expensetracker.wallet.domain.repository.TransactionRepository
import com.expensetracker.wallet.domain.usecase.account.CalculateBalanceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

sealed interface AccountsUiState {
    data object Loading : AccountsUiState
    data class Loaded(val accounts: List<AccountUi>) : AccountsUiState
}

@HiltViewModel
class AccountsViewModel @Inject constructor(
    accountRepository: AccountRepository,
    transactionRepository: TransactionRepository,
    calculateBalance: CalculateBalanceUseCase,
) : ViewModel() {

    // Recomputes whenever accounts *or* transactions change — a transaction added elsewhere
    // doesn't change the accounts table, so observeActiveAccounts() alone wouldn't re-emit.
    val uiState: StateFlow<AccountsUiState> = combine(
        accountRepository.observeActiveAccounts(),
        transactionRepository.observeTransactions(),
    ) { accounts, _ ->
        val items = mutableListOf<AccountUi>()
        for (account in accounts) {
            items += AccountUi(
                id = account.id,
                name = account.name,
                type = account.type,
                currency = account.currency,
                balanceMinor = calculateBalance(account),
            )
        }
        AccountsUiState.Loaded(items)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AccountsUiState.Loading,
    )
}
