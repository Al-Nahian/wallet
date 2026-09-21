package com.example.wallet.feature.accounts

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wallet.domain.model.AccountType
import com.example.wallet.domain.repository.AccountRepository
import com.example.wallet.domain.repository.CategoryRepository
import com.example.wallet.domain.repository.InstitutionRepository
import com.example.wallet.domain.repository.TransactionRepository
import com.example.wallet.domain.usecase.account.ArchiveAccountUseCase
import com.example.wallet.domain.usecase.account.CalculateBalanceUseCase
import com.example.wallet.domain.usecase.transaction.DeleteTransactionUseCase
import com.example.wallet.feature.transactions.TransactionUi
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
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
    val transactions: List<TransactionUi> = emptyList(),
    val errorMessage: String? = null,
)

private const val ACCOUNT_ID_ARG = "accountId"

@HiltViewModel
class AccountDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    accountRepository: AccountRepository,
    transactionRepository: TransactionRepository,
    categoryRepository: CategoryRepository,
    private val institutionRepository: InstitutionRepository,
    private val calculateBalance: CalculateBalanceUseCase,
    private val archiveAccountUseCase: ArchiveAccountUseCase,
    private val deleteTransactionUseCase: DeleteTransactionUseCase,
) : ViewModel() {

    private val accountId: String = checkNotNull(savedStateHandle[ACCOUNT_ID_ARG])

    // Recomputes on this account's own changes *and* on any transaction change against it —
    // a new/edited/deleted transaction doesn't touch the accounts table on its own.
    val uiState: StateFlow<AccountDetailState> = combine(
        accountRepository.observeAccount(accountId),
        transactionRepository.observeByAccount(accountId),
        categoryRepository.observeCategories(),
    ) { account, transactions, categories ->
        if (account == null) {
            AccountDetailState(isLoading = false, errorMessage = "This account no longer exists.")
        } else {
            val categoriesById = categories.associateBy { it.id }
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
                transactions = transactions.sortedByDescending { it.date }.map { tx ->
                    TransactionUi(
                        id = tx.id,
                        type = tx.type,
                        amountMinor = tx.amountMinor,
                        currency = tx.currency,
                        categoryName = tx.categoryId?.let { categoriesById[it]?.name },
                        payee = tx.payee,
                        accountName = account.name,
                        date = tx.date,
                    )
                },
            )
        }
    }.stateIn(
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

    fun deleteTransaction(transactionId: String) {
        viewModelScope.launch { deleteTransactionUseCase(transactionId) }
    }
}
