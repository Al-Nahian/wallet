package com.expensetracker.wallet.feature.labels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expensetracker.wallet.domain.model.Label
import com.expensetracker.wallet.domain.repository.LabelRepository
import com.expensetracker.wallet.domain.usecase.label.CreateLabelUseCase
import com.expensetracker.wallet.domain.usecase.label.DeleteLabelUseCase
import com.expensetracker.wallet.domain.usecase.label.LabelValidationException
import com.expensetracker.wallet.domain.usecase.label.UpdateLabelUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class LabelsViewModel @Inject constructor(
    labelRepository: LabelRepository,
    private val createLabelUseCase: CreateLabelUseCase,
    private val updateLabelUseCase: UpdateLabelUseCase,
    private val deleteLabelUseCase: DeleteLabelUseCase,
) : ViewModel() {

    val labels: StateFlow<List<Label>> = labelRepository.observeLabels().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun dismissError() {
        _errorMessage.value = null
    }

    fun createLabel(name: String) {
        viewModelScope.launch {
            createLabelUseCase(name).onFailure { error -> _errorMessage.value = errorMessageFor(error) }
        }
    }

    fun updateLabel(labelId: String, name: String) {
        viewModelScope.launch {
            updateLabelUseCase(labelId, name).onFailure { error -> _errorMessage.value = errorMessageFor(error) }
        }
    }

    fun deleteLabel(labelId: String) {
        viewModelScope.launch {
            deleteLabelUseCase(labelId).onFailure { error -> _errorMessage.value = errorMessageFor(error) }
        }
    }

    private fun errorMessageFor(error: Throwable): String =
        (error as? LabelValidationException)?.error?.userMessage
            ?: "Something went wrong. Please try again."
}
