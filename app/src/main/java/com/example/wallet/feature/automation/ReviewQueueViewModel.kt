package com.example.wallet.feature.automation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wallet.core.common.minorUnitsToEditableString
import com.example.wallet.core.design.components.SelectorOption
import com.example.wallet.domain.model.AutomationCandidate
import com.example.wallet.domain.model.Category
import com.example.wallet.domain.model.CategoryGroup
import com.example.wallet.domain.repository.AccountRepository
import com.example.wallet.domain.repository.AutomationCandidateRepository
import com.example.wallet.domain.repository.CategoryRepository
import com.example.wallet.domain.usecase.automation.AcceptAutomationCandidateUseCase
import com.example.wallet.domain.usecase.automation.AcceptCandidateError
import com.example.wallet.domain.usecase.automation.AcceptCandidateException
import com.example.wallet.domain.usecase.automation.IgnoreAutomationCandidateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Per-row edits the user makes before accepting — only ever needed when the candidate's own
 * account resolution was ambiguous (null), but editable regardless. */
data class RowOverride(
    val accountId: String? = null,
    val toAccountId: String? = null,
    val categoryId: String? = null,
)

data class ReviewQueueUiState(
    val candidates: List<AutomationCandidate> = emptyList(),
    val accountOptions: List<SelectorOption> = emptyList(),
    val categoryGroups: List<CategoryGroup> = emptyList(),
    val categories: List<Category> = emptyList(),
    val overrides: Map<String, RowOverride> = emptyMap(),
    val errorMessage: String? = null,
) {
    fun accountIdFor(candidate: AutomationCandidate): String? = overrides[candidate.id]?.accountId ?: candidate.accountId
    fun toAccountIdFor(candidate: AutomationCandidate): String? = overrides[candidate.id]?.toAccountId ?: candidate.toAccountId
    fun categoryIdFor(candidate: AutomationCandidate): String? = overrides[candidate.id]?.categoryId ?: candidate.categoryId
}

@HiltViewModel
class ReviewQueueViewModel @Inject constructor(
    private val automationCandidateRepository: AutomationCandidateRepository,
    accountRepository: AccountRepository,
    categoryRepository: CategoryRepository,
    private val acceptAutomationCandidateUseCase: AcceptAutomationCandidateUseCase,
    private val ignoreAutomationCandidateUseCase: IgnoreAutomationCandidateUseCase,
) : ViewModel() {

    private val overrides = MutableStateFlow<Map<String, RowOverride>>(emptyMap())
    private val errorMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<ReviewQueueUiState> = combine(
        automationCandidateRepository.observePending(),
        accountRepository.observeAllAccounts(),
        categoryRepository.observeGroups(),
        categoryRepository.observeCategories(),
    ) { candidates, accounts, groups, categories ->
        ReviewQueueUiState(
            candidates = candidates,
            accountOptions = accounts.map { SelectorOption(it.id, it.name) },
            categoryGroups = groups,
            categories = categories,
        )
    }.combine(overrides) { state, overrideMap -> state.copy(overrides = overrideMap) }
        .combine(errorMessage) { state, message -> state.copy(errorMessage = message) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ReviewQueueUiState())

    fun onAccountChange(candidateId: String, accountId: String) {
        overrides.value = overrides.value + (candidateId to (overrides.value[candidateId] ?: RowOverride()).copy(accountId = accountId))
    }

    fun onToAccountChange(candidateId: String, accountId: String) {
        overrides.value = overrides.value + (candidateId to (overrides.value[candidateId] ?: RowOverride()).copy(toAccountId = accountId))
    }

    fun onCategoryChange(candidateId: String, categoryId: String?) {
        overrides.value = overrides.value + (candidateId to (overrides.value[candidateId] ?: RowOverride()).copy(categoryId = categoryId))
    }

    fun accept(candidate: AutomationCandidate) {
        val state = uiState.value
        val accountId = state.accountIdFor(candidate)
        val toAccountId = state.toAccountIdFor(candidate)
        val categoryId = state.categoryIdFor(candidate)

        viewModelScope.launch {
            errorMessage.value = null
            acceptAutomationCandidateUseCase(
                candidateId = candidate.id,
                accountId = accountId,
                toAccountId = toAccountId,
                categoryId = categoryId,
                amountInput = minorUnitsToEditableString(candidate.amountMinor),
                payee = candidate.payee,
                note = candidate.note,
                date = candidate.date,
            ).onFailure { error -> errorMessage.value = messageFor(error) }
        }
    }

    fun ignore(candidateId: String) {
        viewModelScope.launch { ignoreAutomationCandidateUseCase(candidateId) }
    }

    private fun messageFor(error: Throwable): String {
        val acceptError = (error as? AcceptCandidateException)?.error ?: return "Something went wrong. Please try again."
        return when (acceptError) {
            AcceptCandidateError.CandidateNotFound -> "This candidate no longer exists."
            AcceptCandidateError.AccountRequired -> "Choose an account first."
            AcceptCandidateError.DestinationAccountRequired -> "Choose both accounts for this transfer."
            is AcceptCandidateError.Underlying -> acceptError.message
        }
    }
}
