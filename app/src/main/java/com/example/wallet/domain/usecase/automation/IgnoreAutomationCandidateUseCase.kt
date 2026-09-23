package com.example.wallet.domain.usecase.automation

import com.example.wallet.domain.model.AutomationCandidateStatus
import com.example.wallet.domain.repository.AutomationCandidateRepository
import javax.inject.Inject

class IgnoreAutomationCandidateUseCase @Inject constructor(
    private val automationCandidateRepository: AutomationCandidateRepository,
) {
    suspend operator fun invoke(candidateId: String) {
        automationCandidateRepository.updateStatus(candidateId, AutomationCandidateStatus.IGNORED)
    }
}
