package com.example.wallet.feature.templates

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wallet.domain.model.Account
import com.example.wallet.domain.model.Category
import com.example.wallet.domain.model.Label
import com.example.wallet.domain.model.Template
import com.example.wallet.domain.repository.AccountRepository
import com.example.wallet.domain.repository.CategoryRepository
import com.example.wallet.domain.repository.LabelRepository
import com.example.wallet.domain.repository.TemplateRepository
import com.example.wallet.domain.usecase.template.CreateTemplateUseCase
import com.example.wallet.domain.usecase.template.DeleteTemplateUseCase
import com.example.wallet.domain.usecase.template.TemplateValidationException
import com.example.wallet.domain.usecase.template.UpdateTemplateUseCase
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
) : ViewModel() {

    val uiState: StateFlow<TemplatesUiState> = combine(
        templateRepository.observeTemplates(),
        accountRepository.observeActiveAccounts(),
        categoryRepository.observeCategories(),
        labelRepository.observeLabels(),
    ) { templates, accounts, categories, labels -> TemplatesUiState(templates, accounts, categories, labels) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = TemplatesUiState(),
        )

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
