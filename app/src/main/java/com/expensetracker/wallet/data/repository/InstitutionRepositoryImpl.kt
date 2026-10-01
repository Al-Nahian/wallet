package com.expensetracker.wallet.data.repository

import com.expensetracker.wallet.core.common.newId
import com.expensetracker.wallet.data.local.dao.InstitutionDao
import com.expensetracker.wallet.data.local.entity.InstitutionEntity
import com.expensetracker.wallet.domain.model.Institution
import com.expensetracker.wallet.domain.repository.InstitutionRepository
import javax.inject.Inject

class InstitutionRepositoryImpl @Inject constructor(
    private val institutionDao: InstitutionDao,
) : InstitutionRepository {

    override suspend fun getById(id: String): Institution? = institutionDao.getById(id)?.toDomain()

    override suspend fun findOrCreateByName(name: String): Institution {
        institutionDao.getByName(name)?.let { return it.toDomain() }

        val entity = InstitutionEntity(id = newId(), name = name, type = "OTHER")
        institutionDao.upsert(entity)
        return entity.toDomain()
    }
}
