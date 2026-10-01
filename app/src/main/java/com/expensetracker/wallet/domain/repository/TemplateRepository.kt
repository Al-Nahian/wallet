package com.expensetracker.wallet.domain.repository

import com.expensetracker.wallet.domain.model.Template
import kotlinx.coroutines.flow.Flow

interface TemplateRepository {
    fun observeTemplates(): Flow<List<Template>>
    suspend fun getTemplate(id: String): Template?
    suspend fun create(template: Template)
    suspend fun update(template: Template)
    suspend fun delete(templateId: String)
}
