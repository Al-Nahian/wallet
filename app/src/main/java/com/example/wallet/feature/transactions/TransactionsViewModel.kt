package com.example.wallet.feature.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wallet.core.common.dateGroupLabel
import com.example.wallet.domain.repository.AccountRepository
import com.example.wallet.domain.repository.CategoryRepository
import com.example.wallet.domain.repository.TransactionRepository
import com.example.wallet.domain.repository.TransactionSplitRepository
import com.example.wallet.domain.usecase.transaction.DeleteTransactionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface TransactionsUiState {
    data object Loading : TransactionsUiState
    data class Loaded(val groups: List<TransactionGroupUi>) : TransactionsUiState
}

@HiltViewModel
class TransactionsViewModel @Inject constructor(
    transactionRepository: TransactionRepository,
    accountRepository: AccountRepository,
    categoryRepository: CategoryRepository,
    transactionSplitRepository: TransactionSplitRepository,
    private val deleteTransactionUseCase: DeleteTransactionUseCase,
) : ViewModel() {

    val uiState: StateFlow<TransactionsUiState> = combine(
        transactionRepository.observeTransactions(),
        accountRepository.observeAllAccounts(),
        categoryRepository.observeCategories(),
        transactionSplitRepository.observeAllSplits(),
    ) { transactions, accounts, categories, splits ->
        val accountsById = accounts.associateBy { it.id }
        val categoriesById = categories.associateBy { it.id }
        val splitsByTransaction = splits.groupBy { it.transactionId }
        val transferLegsByTransferId = transactions.filter { it.transferId != null }.groupBy { it.transferId }

        val sorted = transactions.sortedByDescending { it.date }
        val groups = sorted
            .groupBy { dateGroupLabel(it.date) }
            .map { (label, txs) ->
                TransactionGroupUi(
                    dateLabel = label,
                    transactions = txs.map { tx ->
                        tx.toTransactionUi(accountsById, categoriesById, splitsByTransaction, transferLegsByTransferId)
                    },
                )
            }

        TransactionsUiState.Loaded(groups)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TransactionsUiState.Loading,
    )

    fun deleteTransaction(id: String) {
        viewModelScope.launch { deleteTransactionUseCase(id) }
    }
}
