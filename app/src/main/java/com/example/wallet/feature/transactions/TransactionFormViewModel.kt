package com.example.wallet.feature.transactions

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wallet.core.common.minorUnitsToEditableString
import com.example.wallet.core.design.components.applyAmountKeypadKey
import com.example.wallet.domain.model.AccountType
import com.example.wallet.domain.model.Category
import com.example.wallet.domain.model.CategoryGroup
import com.example.wallet.domain.model.Label
import com.example.wallet.domain.model.TransactionType
import com.example.wallet.domain.repository.AccountRepository
import com.example.wallet.domain.repository.CategoryRepository
import com.example.wallet.domain.repository.LabelRepository
import com.example.wallet.domain.repository.TransactionRepository
import com.example.wallet.domain.repository.TransactionSplitRepository
import com.example.wallet.domain.usecase.account.CalculateBalanceUseCase
import com.example.wallet.domain.usecase.budget.CheckBudgetAlertsUseCase
import com.example.wallet.domain.usecase.label.AssignLabelUseCase
import com.example.wallet.domain.usecase.label.CreateLabelUseCase
import com.example.wallet.domain.usecase.label.LabelValidationException
import com.example.wallet.domain.usecase.transaction.CreateTransactionUseCase
import com.example.wallet.domain.usecase.transaction.CreateTransferUseCase
import com.example.wallet.domain.usecase.transaction.DeleteTransactionUseCase
import com.example.wallet.domain.usecase.transaction.TransactionValidationException
import com.example.wallet.domain.usecase.transaction.UpdateTransactionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Kotlin's stdlib only has Pair/Triple — a small local holder for the 5-way `combine` below. */
private data class CategoryFormData(
    val accountOptions: List<AccountPickerOption>,
    val groups: List<CategoryGroup>,
    val categories: List<Category>,
    val labels: List<Label>,
    val payeeSuggestions: List<String>,
    val placeSuggestions: List<String>,
)

/** Ranks past values by how often they recur, then by recency — a payee/place used many times
 * belongs near the top even if a one-off entry happened more recently. Blank/whitespace-only
 * values are dropped (an empty payee/place isn't a suggestion). */
private fun rankSuggestions(values: List<Pair<String?, Long>>, limit: Int = 8): List<String> =
    values
        .mapNotNull { (value, date) -> value?.trim()?.takeIf { it.isNotEmpty() }?.let { it to date } }
        .groupBy({ it.first }, { it.second })
        .map { (value, dates) -> value to (dates.size to dates.max()) }
        .sortedWith(compareByDescending<Pair<String, Pair<Int, Long>>> { it.second.first }.thenByDescending { it.second.second })
        .map { it.first }
        .take(limit)

/** One account in the Select-Account popup: identity plus the live balance the popup shows
 * under each name (computed by [CalculateBalanceUseCase], never stored). */
data class AccountPickerOption(
    val id: String,
    val name: String,
    val type: AccountType,
    val currency: String,
    val balanceMinor: Long,
)

data class TransactionFormState(
    val isEditMode: Boolean = false,
    val isLoading: Boolean = false,
    val type: TransactionType = TransactionType.EXPENSE,
    val accountId: String? = null,
    val toAccountId: String? = null,
    val amountInput: String = "",
    /** The expression just evaluated by "=" (e.g. "120+35"), shown greyed-out above [amountInput]
     * once it holds the result — cleared the moment the user types anything new, same as a
     * calculator's history line. */
    val amountEquation: String? = null,
    val categoryId: String? = null,
    val payee: String = "",
    val note: String = "",
    val place: String = "",
    val date: Long = System.currentTimeMillis(),
    val accountOptions: List<AccountPickerOption> = emptyList(),
    val categoryGroups: List<CategoryGroup> = emptyList(),
    val categories: List<Category> = emptyList(),
    val labelOptions: List<Label> = emptyList(),
    val selectedLabelIds: Set<String> = emptySet(),
    val payeeSuggestions: List<String> = emptyList(),
    val placeSuggestions: List<String> = emptyList(),
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val saved: Boolean = false,
    val isDeleting: Boolean = false,
    val deleted: Boolean = false,
)

private const val TRANSACTION_ID_ARG = "transactionId"

@HiltViewModel
class TransactionFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val transactionRepository: TransactionRepository,
    private val transactionSplitRepository: TransactionSplitRepository,
    private val labelRepository: LabelRepository,
    accountRepository: AccountRepository,
    categoryRepository: CategoryRepository,
    private val createTransactionUseCase: CreateTransactionUseCase,
    private val updateTransactionUseCase: UpdateTransactionUseCase,
    private val createTransferUseCase: CreateTransferUseCase,
    private val calculateBalance: CalculateBalanceUseCase,
    private val assignLabelUseCase: AssignLabelUseCase,
    private val createLabelUseCase: CreateLabelUseCase,
    private val checkBudgetAlertsUseCase: CheckBudgetAlertsUseCase,
    private val deleteTransactionUseCase: DeleteTransactionUseCase,
) : ViewModel() {

    private val transactionId: String? = savedStateHandle[TRANSACTION_ID_ARG]

    private val _uiState = MutableStateFlow(
        TransactionFormState(isEditMode = transactionId != null, isLoading = transactionId != null),
    )
    val uiState: StateFlow<TransactionFormState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            // Transactions join the combine so balances recompute when entries change elsewhere —
            // a transaction added on another screen doesn't touch the accounts table, so
            // observeActiveAccounts() alone wouldn't re-emit (same pattern as AccountsViewModel).
            combine(
                accountRepository.observeActiveAccounts(),
                transactionRepository.observeTransactions(),
                categoryRepository.observeGroups(),
                categoryRepository.observeCategories(),
                labelRepository.observeLabels(),
            ) { accounts, transactions, groups, categories, labels ->
                CategoryFormData(
                    accounts.map { account ->
                        AccountPickerOption(
                            id = account.id,
                            name = account.name,
                            type = account.type,
                            currency = account.currency,
                            balanceMinor = calculateBalance(account),
                        )
                    },
                    groups,
                    categories,
                    labels,
                    payeeSuggestions = rankSuggestions(transactions.map { it.payee to it.date }),
                    placeSuggestions = rankSuggestions(transactions.map { it.place to it.date }),
                )
            }.collect { data ->
                _uiState.update {
                    it.copy(
                        accountOptions = data.accountOptions,
                        categoryGroups = data.groups,
                        categories = data.categories,
                        labelOptions = data.labels,
                        payeeSuggestions = data.payeeSuggestions,
                        placeSuggestions = data.placeSuggestions,
                    )
                }
            }
        }

        transactionId?.let(::loadExistingTransaction)
    }

    private fun loadExistingTransaction(id: String) {
        viewModelScope.launch {
            val transaction = transactionRepository.getTransaction(id)
            if (transaction == null) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Transaction not found.") }
                return@launch
            }
            if (transaction.type == TransactionType.TRANSFER) {
                // Transfers are two linked rows (plan.md §22) with no dedicated edit path yet —
                // blocked at the screens that navigate here too (plans/06-transfers.md).
                _uiState.update { it.copy(isLoading = false, errorMessage = "Transfers can't be edited yet.") }
                return@launch
            }
            val existingSplits = transactionSplitRepository.observeByTransaction(id).first()
            if (existingSplits.isNotEmpty()) {
                // Split entry was removed from the form — a legacy split parent has no single
                // category to show, so it can't be edited here (same treatment as transfers).
                _uiState.update { it.copy(isLoading = false, errorMessage = "Split transactions can't be edited.") }
                return@launch
            }
            val existingLabels = labelRepository.observeLabelsForTransaction(id).first()
            _uiState.update {
                it.copy(
                    isLoading = false,
                    type = transaction.type,
                    accountId = transaction.accountId,
                    amountInput = minorUnitsToEditableString(kotlin.math.abs(transaction.amountMinor)),
                    categoryId = transaction.categoryId,
                    payee = transaction.payee.orEmpty(),
                    note = transaction.note.orEmpty(),
                    place = transaction.place.orEmpty(),
                    date = transaction.date,
                    selectedLabelIds = existingLabels.map { it.id }.toSet(),
                )
            }
        }
    }

    fun onTypeChange(type: TransactionType) = _uiState.update { it.copy(type = type, errorMessage = null) }
    fun onAccountChange(id: String) = _uiState.update {
        it.copy(accountId = id, toAccountId = it.toAccountId.takeUnless { to -> to == id }, errorMessage = null)
    }
    fun onToAccountChange(id: String) = _uiState.update { it.copy(toAccountId = id, errorMessage = null) }
    fun onAmountChange(value: String) = _uiState.update { it.copy(amountInput = value, amountEquation = null, errorMessage = null) }
    fun onAmountKeypadKey(key: String) = _uiState.update {
        // Right after "=" shows a result, "⌫" clears the whole thing back to the default "0.00"
        // instead of deleting one digit off the result — there's no expression left to edit at
        // that point, only a finished answer, so a full reset reads more like "clear" than
        // "backspace one character into a stale result."
        if (key == "⌫" && it.amountEquation != null) {
            return@update it.copy(amountInput = "", amountEquation = null, errorMessage = null)
        }
        // Right after "=" shows a result, a fresh digit/"." starts an entirely new number instead
        // of appending to that result — standard calculator behavior. An operator instead chains
        // off the result (e.g. result "+" continues a running total), so only digits/"." reset.
        val isDigitOrDot = key.length == 1 && (key[0].isDigit() || key == ".")
        val base = if (it.amountEquation != null && isDigitOrDot) "" else it.amountInput
        val next = applyAmountKeypadKey(base, key)
        val equation = if (key == "=" && next != base) base else null
        it.copy(amountInput = next, amountEquation = equation, errorMessage = null)
    }
    fun onCategoryChange(id: String?) = _uiState.update { it.copy(categoryId = id) }
    fun onPayeeChange(value: String) = _uiState.update { it.copy(payee = value) }
    fun onNoteChange(value: String) = _uiState.update { it.copy(note = value) }
    fun onPlaceChange(value: String) = _uiState.update { it.copy(place = value) }
    fun onDateChange(value: Long) = _uiState.update { it.copy(date = value) }

    fun clearError() = _uiState.update { it.copy(errorMessage = null) }

    fun onToggleLabel(labelId: String) = _uiState.update {
        val current = it.selectedLabelIds
        it.copy(selectedLabelIds = if (labelId in current) current - labelId else current + labelId)
    }

    /** Creates and immediately selects a new label from the Labels picker dialog — lets the user
     * add one without leaving the transaction form for Profile > Manage labels. [labelOptions]
     * picks it up on its own via the [labelRepository.observeLabels] flow this init already
     * collects. */
    fun createLabel(name: String) {
        viewModelScope.launch {
            createLabelUseCase(name)
                .onSuccess { label ->
                    _uiState.update { it.copy(selectedLabelIds = it.selectedLabelIds + label.id) }
                }
                .onFailure { error ->
                    val message = (error as? LabelValidationException)?.error?.userMessage
                        ?: "Couldn't create that label. Please try again."
                    _uiState.update { it.copy(errorMessage = message) }
                }
        }
    }

    fun save() {
        val state = _uiState.value
        if (state.isSaving) return

        if (state.type == TransactionType.TRANSFER) {
            saveTransfer(state)
            return
        }

        val accountId = state.accountId
        if (accountId == null) {
            _uiState.update { it.copy(errorMessage = "Please select an account.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }

            val result = if (state.isEditMode && transactionId != null) {
                updateTransactionUseCase(
                    transactionId = transactionId,
                    type = state.type,
                    accountId = accountId,
                    amountInput = state.amountInput,
                    categoryId = state.categoryId,
                    payee = state.payee,
                    note = state.note,
                    date = state.date,
                    place = state.place,
                )
            } else {
                createTransactionUseCase(
                    type = state.type,
                    accountId = accountId,
                    amountInput = state.amountInput,
                    categoryId = state.categoryId,
                    payee = state.payee,
                    note = state.note,
                    date = state.date,
                    place = state.place,
                )
            }

            result.fold(
                onSuccess = { transaction ->
                    assignLabelUseCase(transaction.id, state.selectedLabelIds)
                    if (transaction.type == TransactionType.EXPENSE) checkBudgetAlertsUseCase()
                    _uiState.update { it.copy(isSaving = false, saved = true) }
                },
                onFailure = { error -> _uiState.update { it.copy(isSaving = false, errorMessage = errorMessageFor(error)) } },
            )
        }
    }

    /** plan.md §22 — a transfer has no category/payee/split/labels, and (for now) no edit
     * support (the two linked legs have no atomic "update pair" path yet — plans/06-transfers.md
     * only built create + pair-aware delete), so this only ever creates. */
    private fun saveTransfer(state: TransactionFormState) {
        if (state.isEditMode) {
            _uiState.update { it.copy(errorMessage = "Transfers can't be edited yet.") }
            return
        }
        val fromAccountId = state.accountId
        val toAccountId = state.toAccountId
        if (fromAccountId == null || toAccountId == null) {
            _uiState.update { it.copy(errorMessage = "Please select both accounts.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }

            val result = createTransferUseCase(
                fromAccountId = fromAccountId,
                toAccountId = toAccountId,
                amountInput = state.amountInput,
                note = state.note,
                date = state.date,
            )

            result.fold(
                onSuccess = { _uiState.update { it.copy(isSaving = false, saved = true) } },
                onFailure = { error -> _uiState.update { it.copy(isSaving = false, errorMessage = errorMessageFor(error)) } },
            )
        }
    }

    /** The "undo" affordance a `TRANSACTION_CAPTURED` automation notification deep-links to
     * (plans/14-sms-notification-automation.md's acceptance criteria) — soft-deletes exactly
     * like the swipe-to-delete action on the Transactions list, just reachable from here too. */
    fun delete() {
        val id = transactionId ?: return
        if (_uiState.value.isDeleting) return
        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true, errorMessage = null) }
            deleteTransactionUseCase(id).fold(
                onSuccess = { _uiState.update { it.copy(isDeleting = false, deleted = true) } },
                onFailure = { error -> _uiState.update { it.copy(isDeleting = false, errorMessage = errorMessageFor(error)) } },
            )
        }
    }

    private fun errorMessageFor(error: Throwable): String =
        (error as? TransactionValidationException)?.error?.userMessage
            ?: "We couldn't save this transaction. Please try again."
}
