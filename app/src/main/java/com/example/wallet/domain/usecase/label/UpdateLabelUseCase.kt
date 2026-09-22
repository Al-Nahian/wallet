package com.example.wallet.domain.usecase.label

import com.example.wallet.domain.model.Label
import com.example.wallet.domain.repository.LabelRepository
import javax.inject.Inject

class UpdateLabelUseCase @Inject constructor(
    private val labelRepository: LabelRepository,
) {
    suspend operator fun invoke(labelId: String, name: String, color: String): Result<Label> {
        val existing = labelRepository.getLabel(labelId)
            ?: return Result.failure(LabelValidationException(LabelError.LabelNotFound))
        val cleanName = name.trim()
        if (cleanName.isEmpty()) {
            return Result.failure(LabelValidationException(LabelError.NameRequired))
        }

        val updated = existing.copy(name = cleanName, color = color)
        labelRepository.update(updated)
        return Result.success(updated)
    }
}
