package com.expensetracker.wallet.data.repository

import androidx.room.withTransaction
import com.expensetracker.wallet.core.database.AppDatabase
import com.expensetracker.wallet.data.local.dao.TemplateDao
import com.expensetracker.wallet.domain.model.Template
import com.expensetracker.wallet.domain.repository.TemplateRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TemplateRepositoryImpl @Inject constructor(
    private val appDatabase: AppDatabase,
    private val templateDao: TemplateDao,
) : TemplateRepository {

    override fun observeTemplates(): Flow<List<Template>> =
        templateDao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override suspend fun getTemplate(id: String): Template? = templateDao.getById(id)?.toDomain()

    override suspend fun create(template: Template) {
        appDatabase.withTransaction { templateDao.insert(template.toEntity()) }
    }

    override suspend fun update(template: Template) {
        appDatabase.withTransaction { templateDao.update(template.toEntity()) }
    }

    override suspend fun delete(templateId: String) {
        val entity = templateDao.getById(templateId) ?: return
        appDatabase.withTransaction { templateDao.delete(entity) }
    }
}
