package com.example.wallet.domain.usecase.label

import com.example.wallet.core.common.newId
import com.example.wallet.domain.model.Label
import com.example.wallet.domain.repository.LabelRepository
import javax.inject.Inject

/** plan.md §16 — e.g. "Family", "Work", "Vacation". */
class CreateLabelUseCase @Inject constructor(
    private val labelRepository: LabelRepository,
) {
    suspend operator fun invoke(name: String, color: String): Result<Label> {
        val cleanName = name.trim()
        if (cleanName.isEmpty()) {
            return Result.failure(LabelValidationException(LabelError.NameRequired))
        }

        val label = Label(id = newId(), name = cleanName, color = color, createdAt = System.currentTimeMillis())
        labelRepository.create(label)
        return Result.success(label)
    }
}
