package com.expensetracker.wallet.domain.usecase.account

import com.expensetracker.wallet.domain.model.Institution
import com.expensetracker.wallet.domain.repository.InstitutionRepository

class FakeInstitutionRepository : InstitutionRepository {
    private val institutions = mutableMapOf<String, Institution>()
    private var nextId = 0

    override suspend fun getById(id: String): Institution? = institutions[id]

    override suspend fun findOrCreateByName(name: String): Institution {
        institutions.values.firstOrNull { it.name.equals(name, ignoreCase = true) }?.let { return it }

        val institution = Institution(
            id = "institution-${nextId++}",
            name = name,
            type = "OTHER",
            logo = null,
            country = null,
        )
        institutions[institution.id] = institution
        return institution
    }
}
