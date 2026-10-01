package com.expensetracker.wallet.feature.templates

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expensetracker.wallet.domain.model.Category
import com.expensetracker.wallet.domain.model.Template
import com.expensetracker.wallet.domain.repository.CategoryRepository
import com.expensetracker.wallet.domain.repository.TemplateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/** Feeds the Home FAB's [com.expensetracker.wallet.core.design.components.TemplateFabMenu] — split out
 * from [TemplatesViewModel] since the FAB only ever needs the bare template list (plus categories,
 * to pick each template's icon by its saved category), not the accounts/labels the full Manage
 * Templates screen also loads. */
@HiltViewModel
class TemplateShortcutsViewModel @Inject constructor(
    templateRepository: TemplateRepository,
    categoryRepository: CategoryRepository,
) : ViewModel() {
    val templates: StateFlow<List<Template>> = templateRepository.observeTemplates()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )
    val categories: StateFlow<List<Category>> = categoryRepository.observeCategories()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )
}
