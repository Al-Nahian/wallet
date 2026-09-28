package com.example.wallet.domain.usecase.label

import com.example.wallet.domain.model.Label
import com.example.wallet.domain.repository.LabelRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.first

class UpdateLabelUseCase @Inject constructor(
    private val labelRepository: LabelRepository,
) {
    suspend operator fun invoke(labelId: String, name: String): Result<Label> {
        val existing = labelRepository.getLabel(labelId)
            ?: return Result.failure(LabelValidationException(LabelError.LabelNotFound))
        val cleanName = name.trim()
        if (cleanName.isEmpty()) {
            return Result.failure(LabelValidationException(LabelError.NameRequired))
        }

        val isDuplicate = labelRepository.observeLabels().first()
            .any { it.id != labelId && it.name.equals(cleanName, ignoreCase = true) }
        if (isDuplicate) {
            return Result.failure(LabelValidationException(LabelError.DuplicateName))
        }

        val updated = existing.copy(name = cleanName)
        labelRepository.update(updated)
        return Result.success(updated)
    }
}
