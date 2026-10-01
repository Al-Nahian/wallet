package com.expensetracker.wallet.data.repository

import com.expensetracker.wallet.data.local.dao.AutomationCandidateDao
import com.expensetracker.wallet.domain.model.AutomationCandidate
import com.expensetracker.wallet.domain.model.AutomationCandidateStatus
import com.expensetracker.wallet.domain.repository.AutomationCandidateRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AutomationCandidateRepositoryImpl @Inject constructor(
    private val dao: AutomationCandidateDao,
) : AutomationCandidateRepository {

    override fun observePending(): Flow<List<AutomationCandidate>> =
        dao.observePending().map { entities -> entities.map { it.toDomain() } }

    override fun observePendingCount(): Flow<Int> = dao.observePendingCount()

    override suspend fun getById(id: String): AutomationCandidate? = dao.getById(id)?.toDomain()

    override suspend fun findBySourceReference(sourceReference: String): AutomationCandidate? =
        dao.findBySourceReference(sourceReference)?.toDomain()

    override suspend fun create(candidate: AutomationCandidate) = dao.insert(candidate.toEntity())

    override suspend fun updateStatus(id: String, status: AutomationCandidateStatus) =
        dao.updateStatus(id, status)
}
