package com.example.wallet.domain.usecase.template

import com.example.wallet.domain.model.Template
import com.example.wallet.domain.repository.TemplateRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/** In-memory fake so use-case tests don't need Room/Android. */
class FakeTemplateRepository : TemplateRepository {
    private val templates = MutableStateFlow<Map<String, Template>>(emptyMap())

    override fun observeTemplates(): Flow<List<Template>> = templates.map { it.values.toList() }

    override suspend fun getTemplate(id: String): Template? = templates.value[id]

    override suspend fun create(template: Template) {
        templates.value = templates.value + (template.id to template)
    }

    override suspend fun update(template: Template) {
        templates.value = templates.value + (template.id to template)
    }

    override suspend fun delete(templateId: String) {
        templates.value = templates.value - templateId
    }
}
