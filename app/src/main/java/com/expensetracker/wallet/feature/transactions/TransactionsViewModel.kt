package com.expensetracker.wallet.feature.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expensetracker.wallet.core.common.dateGroupLabel
import com.expensetracker.wallet.domain.repository.AccountRepository
import com.expensetracker.wallet.domain.repository.CategoryRepository
import com.expensetracker.wallet.domain.repository.LabelRepository
import com.expensetracker.wallet.domain.repository.TransactionRepository
import com.expensetracker.wallet.domain.repository.TransactionSplitRepository
import com.expensetracker.wallet.domain.usecase.transaction.DeleteTransactionUseCase
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
    labelRepository: LabelRepository,
    private val deleteTransactionUseCase: DeleteTransactionUseCase,
) : ViewModel() {

    val uiState: StateFlow<TransactionsUiState> = combine(
        transactionRepository.observeTransactions(),
        accountRepository.observeAllAccounts(),
        categoryRepository.observeCategories(),
        transactionSplitRepository.observeAllSplits(),
        labelRepository.observeAllTransactionLabels(),
    ) { transactions, accounts, categories, splits, labelsByTransaction ->
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
                        tx.toTransactionUi(accountsById, categoriesById, splitsByTransaction, transferLegsByTransferId, labelsByTransaction)
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
