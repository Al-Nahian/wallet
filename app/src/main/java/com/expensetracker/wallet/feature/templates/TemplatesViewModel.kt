package com.expensetracker.wallet.feature.templates

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expensetracker.wallet.domain.model.Account
import com.expensetracker.wallet.domain.model.Category
import com.expensetracker.wallet.domain.model.CategoryGroup
import com.expensetracker.wallet.domain.model.Label
import com.expensetracker.wallet.domain.model.Template
import com.expensetracker.wallet.domain.repository.AccountRepository
import com.expensetracker.wallet.domain.repository.CategoryRepository
import com.expensetracker.wallet.domain.repository.LabelRepository
import com.expensetracker.wallet.domain.repository.TemplateRepository
import com.expensetracker.wallet.domain.usecase.label.CreateLabelUseCase
import com.expensetracker.wallet.domain.usecase.template.CreateTemplateUseCase
import com.expensetracker.wallet.domain.usecase.template.DeleteTemplateUseCase
import com.expensetracker.wallet.domain.usecase.template.TemplateValidationException
import com.expensetracker.wallet.domain.usecase.template.UpdateTemplateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class TemplatesUiState(
    val templates: List<Template> = emptyList(),
    val accounts: List<Account> = emptyList(),
    val categories: List<Category> = emptyList(),
    val categoryGroups: List<CategoryGroup> = emptyList(),
    val labels: List<Label> = emptyList(),
)

@HiltViewModel
class TemplatesViewModel @Inject constructor(
    templateRepository: TemplateRepository,
    accountRepository: AccountRepository,
    categoryRepository: CategoryRepository,
    labelRepository: LabelRepository,
    private val createTemplateUseCase: CreateTemplateUseCase,
    private val updateTemplateUseCase: UpdateTemplateUseCase,
    private val deleteTemplateUseCase: DeleteTemplateUseCase,
    private val createLabelUseCase: CreateLabelUseCase,
) : ViewModel() {

    val uiState: StateFlow<TemplatesUiState> = combine(
        templateRepository.observeTemplates(),
        accountRepository.observeActiveAccounts(),
        categoryRepository.observeCategories(),
        categoryRepository.observeGroups(),
        labelRepository.observeLabels(),
    ) { templates, accounts, categories, categoryGroups, labels ->
        TemplatesUiState(templates, accounts, categories, categoryGroups, labels)
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = TemplatesUiState(),
        )

    /** Lets the template form's own label picker create a label inline (plan.md §16's "Add
     * label" affordance, same [CreateLabelUseCase] the transaction form uses) without leaving the
     * dialog — suspend + Result rather than fire-and-forget, so the caller can auto-select the
     * new label and show a validation error (e.g. a duplicate name) right there. */
    suspend fun createLabel(name: String) = createLabelUseCase(name)

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun dismissError() {
        _errorMessage.value = null
    }

    fun createTemplate(name: String, accountId: String?, categoryId: String?, labelId: String?, payee: String?, place: String?) {
        viewModelScope.launch {
            createTemplateUseCase(name, accountId, categoryId, labelId, payee, place)
                .onFailure { error -> _errorMessage.value = errorMessageFor(error) }
        }
    }

    fun updateTemplate(
        templateId: String,
        name: String,
        accountId: String?,
        categoryId: String?,
        labelId: String?,
        payee: String?,
        place: String?,
    ) {
        viewModelScope.launch {
            updateTemplateUseCase(templateId, name, accountId, categoryId, labelId, payee, place)
                .onFailure { error -> _errorMessage.value = errorMessageFor(error) }
        }
    }

    fun deleteTemplate(templateId: String) {
        viewModelScope.launch {
            deleteTemplateUseCase(templateId).onFailure { error -> _errorMessage.value = errorMessageFor(error) }
        }
    }

    private fun errorMessageFor(error: Throwable): String =
        (error as? TemplateValidationException)?.error?.userMessage
            ?: "Something went wrong. Please try again."
}
