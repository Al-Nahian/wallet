package com.expensetracker.wallet.domain.repository

import com.expensetracker.wallet.domain.model.AutomationCandidate
import com.expensetracker.wallet.domain.model.AutomationCandidateStatus
import kotlinx.coroutines.flow.Flow

/** plans/14-sms-notification-automation.md — the Review Queue's backing repository. */
interface AutomationCandidateRepository {
    fun observePending(): Flow<List<AutomationCandidate>>
    fun observePendingCount(): Flow<Int>
    suspend fun getById(id: String): AutomationCandidate?
    suspend fun findBySourceReference(sourceReference: String): AutomationCandidate?
    suspend fun create(candidate: AutomationCandidate)
    suspend fun updateStatus(id: String, status: AutomationCandidateStatus)
}
