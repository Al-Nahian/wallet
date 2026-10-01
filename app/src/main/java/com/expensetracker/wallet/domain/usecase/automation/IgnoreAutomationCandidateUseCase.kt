package com.expensetracker.wallet.domain.usecase.automation

import com.expensetracker.wallet.domain.model.AutomationCandidateStatus
import com.expensetracker.wallet.domain.repository.AutomationCandidateRepository
import javax.inject.Inject

class IgnoreAutomationCandidateUseCase @Inject constructor(
    private val automationCandidateRepository: AutomationCandidateRepository,
) {
    suspend operator fun invoke(candidateId: String) {
        automationCandidateRepository.updateStatus(candidateId, AutomationCandidateStatus.IGNORED)
    }
}
