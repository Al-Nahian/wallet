package com.example.wallet.feature.templates

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wallet.domain.model.Template
import com.example.wallet.domain.repository.TemplateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/** Feeds the Home FAB's [com.example.wallet.core.design.components.TemplateFabMenu] — split out
 * from [TemplatesViewModel] since the FAB only ever needs the bare template list, not the
 * accounts/categories/labels the full Manage Templates screen also loads. */
@HiltViewModel
class TemplateShortcutsViewModel @Inject constructor(
    templateRepository: TemplateRepository,
) : ViewModel() {
    val templates: StateFlow<List<Template>> = templateRepository.observeTemplates()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )
}
