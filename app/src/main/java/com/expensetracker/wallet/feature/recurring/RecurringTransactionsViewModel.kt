package com.expensetracker.wallet.feature.recurring

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expensetracker.wallet.domain.model.RecurringFrequency
import com.expensetracker.wallet.domain.model.TransactionType
import com.expensetracker.wallet.domain.repository.AccountRepository
import com.expensetracker.wallet.domain.repository.CategoryRepository
import com.expensetracker.wallet.domain.repository.RecurringTransactionRepository
import com.expensetracker.wallet.domain.usecase.recurring.DeleteRecurringTransactionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Display-ready row for the recurring list — resolves the rule's raw `accountId`/`categoryId`
 * into names so the screen never has to look them up itself. */
data class RecurringTransactionUi(
    val id: String,
    val accountName: String,
    val categoryName: String?,
    val amountMinor: Long,
    val currency: String,
    val type: TransactionType,
    val frequency: RecurringFrequency,
    val nextDate: Long,
    val payee: String?,
    val autoPost: Boolean,
)

fun RecurringFrequency.label(): String = when (this) {
    RecurringFrequency.DAILY -> "Daily"
    RecurringFrequency.WEEKLY -> "Weekly"
    RecurringFrequency.MONTHLY -> "Monthly"
    RecurringFrequency.YEARLY -> "Yearly"
}

sealed interface RecurringTransactionsUiState {
    data object Loading : RecurringTransactionsUiState
    data class Loaded(val items: List<RecurringTransactionUi>) : RecurringTransactionsUiState
}

@HiltViewModel
class RecurringTransactionsViewModel @Inject constructor(
    private val recurringTransactionRepository: RecurringTransactionRepository,
    accountRepository: AccountRepository,
    categoryRepository: CategoryRepository,
    private val deleteRecurringTransactionUseCase: DeleteRecurringTransactionUseCase,
) : ViewModel() {

    val uiState: StateFlow<RecurringTransactionsUiState> = combine(
        recurringTransactionRepository.observeActive(),
        accountRepository.observeAllAccounts(),
        categoryRepository.observeCategories(),
    ) { recurring, accounts, categories ->
        val accountsById = accounts.associateBy { it.id }
        val categoriesById = categories.associateBy { it.id }
        val items = recurring
            .sortedBy { it.nextDate }
            .map { rule ->
                RecurringTransactionUi(
                    id = rule.id,
                    accountName = accountsById[rule.accountId]?.name ?: "Unknown account",
                    categoryName = rule.categoryId?.let { categoriesById[it]?.name },
                    amountMinor = rule.amountMinor,
                    currency = rule.currency,
                    type = rule.type,
                    frequency = rule.frequency,
                    nextDate = rule.nextDate,
                    payee = rule.payee,
                    autoPost = rule.autoPost,
                )
            }
        RecurringTransactionsUiState.Loaded(items)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = RecurringTransactionsUiState.Loading,
    )

    fun delete(id: String) {
        viewModelScope.launch { deleteRecurringTransactionUseCase(id) }
    }
}
