package com.example.wallet.feature.accounts

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wallet.domain.model.AccountType
import com.example.wallet.domain.repository.AccountRepository
import com.example.wallet.domain.repository.InstitutionRepository
import com.example.wallet.domain.usecase.account.ArchiveAccountUseCase
import com.example.wallet.domain.usecase.account.CalculateBalanceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AccountDetailUi(
    val id: String,
    val name: String,
    val type: AccountType,
    val institutionName: String?,
    val currency: String,
    val balanceMinor: Long,
)

data class AccountDetailState(
    val isLoading: Boolean = true,
    val account: AccountDetailUi? = null,
    val errorMessage: String? = null,
)

private const val ACCOUNT_ID_ARG = "accountId"

@HiltViewModel
class AccountDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    accountRepository: AccountRepository,
    private val institutionRepository: InstitutionRepository,
    private val calculateBalance: CalculateBalanceUseCase,
    private val archiveAccountUseCase: ArchiveAccountUseCase,
) : ViewModel() {

    private val accountId: String = checkNotNull(savedStateHandle[ACCOUNT_ID_ARG])

    val uiState: StateFlow<AccountDetailState> = accountRepository.observeAccount(accountId)
        .map { account ->
            if (account == null) {
                AccountDetailState(isLoading = false, errorMessage = "This account no longer exists.")
            } else {
                val institutionName = account.institutionId?.let { institutionRepository.getById(it)?.name }
                AccountDetailState(
                    isLoading = false,
                    account = AccountDetailUi(
                        id = account.id,
                        name = account.name,
                        type = account.type,
                        institutionName = institutionName,
                        currency = account.currency,
                        balanceMinor = calculateBalance(account),
                    ),
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AccountDetailState(),
        )

    fun archive(onDone: () -> Unit) {
        viewModelScope.launch {
            archiveAccountUseCase(accountId)
            onDone()
        }
    }
}
