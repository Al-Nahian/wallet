package com.expensetracker.wallet.feature.automation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expensetracker.wallet.core.automation.AutomationSettingsRepository
import com.expensetracker.wallet.domain.repository.AutomationCandidateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AutomationSettingsUiState(
    val smsCaptureEnabled: Boolean = false,
    val automaticAddEnabled: Boolean = true,
    val pendingReviewCount: Int = 0,
)

@HiltViewModel
class AutomationSettingsViewModel @Inject constructor(
    private val automationSettingsRepository: AutomationSettingsRepository,
    automationCandidateRepository: AutomationCandidateRepository,
) : ViewModel() {

    val uiState: StateFlow<AutomationSettingsUiState> = combine(
        automationSettingsRepository.isSmsCaptureEnabled,
        automationSettingsRepository.isAutomaticAddEnabled,
        automationCandidateRepository.observePendingCount(),
    ) { smsCaptureEnabled, automaticAddEnabled, pendingCount ->
        AutomationSettingsUiState(smsCaptureEnabled, automaticAddEnabled, pendingCount)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AutomationSettingsUiState())

    /** Called only after the caller has confirmed `RECEIVE_SMS` is actually granted (or the user
     * is turning capture off, which needs no permission) — this repository has no opinion on
     * permissions, just the user's stored preference. */
    fun setSmsCaptureEnabled(enabled: Boolean) {
        viewModelScope.launch { automationSettingsRepository.setSmsCaptureEnabled(enabled) }
    }

    fun setAutomaticAddEnabled(enabled: Boolean) {
        viewModelScope.launch { automationSettingsRepository.setAutomaticAddEnabled(enabled) }
    }
}
