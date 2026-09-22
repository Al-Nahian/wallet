package com.example.wallet.feature.accounts

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wallet.domain.model.Account
import com.example.wallet.domain.model.AccountType
import com.example.wallet.domain.model.Category
import com.example.wallet.domain.model.Transaction
import com.example.wallet.domain.model.TransactionSplit
import com.example.wallet.domain.repository.AccountRepository
import com.example.wallet.domain.repository.CategoryRepository
import com.example.wallet.domain.repository.InstitutionRepository
import com.example.wallet.domain.repository.LabelRepository
import com.example.wallet.domain.repository.TransactionRepository
import com.example.wallet.domain.repository.TransactionSplitRepository
import com.example.wallet.domain.usecase.account.ArchiveAccountUseCase
import com.example.wallet.domain.usecase.account.CalculateBalanceUseCase
import com.example.wallet.domain.usecase.transaction.DeleteTransactionUseCase
import com.example.wallet.feature.transactions.TransactionUi
import com.example.wallet.feature.transactions.toTransactionUi
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

/** Intermediate bundle of the first five combined flows — `combine` only has fixed-arity
 * overloads up to 5, and labels make a 6th, so it's nested via a plain 2-arg `combine` below. */
private data class AccountDetailInputs(
    val account: Account?,
    val allTransactions: List<Transaction>,
    val allAccounts: List<Account>,
    val categories: List<Category>,
    val splits: List<TransactionSplit>,
)

private const val ACCOUNT_ID_ARG = "accountId"

@HiltViewModel
class AccountDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    accountRepository: AccountRepository,
    transactionRepository: TransactionRepository,
    categoryRepository: CategoryRepository,
    transactionSplitRepository: TransactionSplitRepository,
    labelRepository: LabelRepository,
    private val institutionRepository: InstitutionRepository,
    private val calculateBalance: CalculateBalanceUseCase,
    private val archiveAccountUseCase: ArchiveAccountUseCase,
    private val deleteTransactionUseCase: DeleteTransactionUseCase,
) : ViewModel() {

    private val accountId: String = checkNotNull(savedStateHandle[ACCOUNT_ID_ARG])

    // Recomputes on this account's own changes *and* on any transaction/split/label change
    // anywhere — a new/edited/deleted transaction doesn't touch the accounts table on its own,
    // and a transfer's linked leg or a split's category name can live on a *different* row.
    val uiState: StateFlow<AccountDetailState> = combine(
        accountRepository.observeAccount(accountId),
        transactionRepository.observeTransactions(),
        accountRepository.observeAllAccounts(),
        categoryRepository.observeCategories(),
        transactionSplitRepository.observeAllSplits(),
    ) { account, allTransactions, allAccounts, categories, splits ->
        AccountDetailInputs(account, allTransactions, allAccounts, categories, splits)
    }.combine(labelRepository.observeAllTransactionLabels()) { inputs, labelsByTransaction ->
        val account = inputs.account
        if (account == null) {
            AccountDetailState(isLoading = false, errorMessage = "This account no longer exists.")
        } else {
            val accountsById = inputs.allAccounts.associateBy { it.id }
            val categoriesById = inputs.categories.associateBy { it.id }
            val splitsByTransaction = inputs.splits.groupBy { it.transactionId }
            val transferLegsByTransferId = inputs.allTransactions.filter { it.transferId != null }.groupBy { it.transferId }
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
                transactions = inputs.allTransactions
                    .filter { it.accountId == accountId }
                    .sortedByDescending { it.date }
                    .map { tx ->
                        tx.toTransactionUi(accountsById, categoriesById, splitsByTransaction, transferLegsByTransferId, labelsByTransaction)
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
