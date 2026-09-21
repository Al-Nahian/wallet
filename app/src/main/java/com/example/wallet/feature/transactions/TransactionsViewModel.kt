package com.example.wallet.feature.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wallet.core.common.dateGroupLabel
import com.example.wallet.domain.repository.AccountRepository
import com.example.wallet.domain.repository.CategoryRepository
import com.example.wallet.domain.repository.TransactionRepository
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
    private val deleteTransactionUseCase: DeleteTransactionUseCase,
) : ViewModel() {

    val uiState: StateFlow<TransactionsUiState> = combine(
        transactionRepository.observeTransactions(),
        accountRepository.observeAllAccounts(),
        categoryRepository.observeCategories(),
    ) { transactions, accounts, categories ->
        val accountsById = accounts.associateBy { it.id }
        val categoriesById = categories.associateBy { it.id }

        val sorted = transactions.sortedByDescending { it.date }
        val groups = sorted
            .groupBy { dateGroupLabel(it.date) }
            .map { (label, txs) ->
                TransactionGroupUi(
                    dateLabel = label,
                    transactions = txs.map { tx ->
                        TransactionUi(
                            id = tx.id,
                            type = tx.type,
                            amountMinor = tx.amountMinor,
                            currency = tx.currency,
                            categoryName = tx.categoryId?.let { categoriesById[it]?.name },
                            payee = tx.payee,
                            accountName = accountsById[tx.accountId]?.name ?: "Unknown account",
                            date = tx.date,
                        )
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
