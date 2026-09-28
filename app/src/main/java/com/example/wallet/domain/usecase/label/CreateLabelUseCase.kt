package com.example.wallet.domain.usecase.label

import com.example.wallet.core.common.newId
import com.example.wallet.domain.model.Label
import com.example.wallet.domain.repository.LabelRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.first

/** A label's [Label.color] field only exists for storage/CSV-format compatibility — labels
 * themselves carry no visible color (that's a category-only concept, plan.md §69 rule 8), so
 * every label gets this same neutral value rather than a user-picked one. */
internal const val NeutralLabelColor = "#9E9E9E"

/** plan.md §16 — e.g. "Family", "Work", "Vacation". */
class CreateLabelUseCase @Inject constructor(
    private val labelRepository: LabelRepository,
) {
    suspend operator fun invoke(name: String): Result<Label> {
        val cleanName = name.trim()
        if (cleanName.isEmpty()) {
            return Result.failure(LabelValidationException(LabelError.NameRequired))
        }

        // Case-insensitive so "Groceries" and "groceries" can't both exist — the picker's own
        // "Add label" shortcut makes it easy to create the same one twice without checking the
        // existing list first.
        val isDuplicate = labelRepository.observeLabels().first().any { it.name.equals(cleanName, ignoreCase = true) }
        if (isDuplicate) {
            return Result.failure(LabelValidationException(LabelError.DuplicateName))
        }

        val label = Label(id = newId(), name = cleanName, color = NeutralLabelColor, createdAt = System.currentTimeMillis())
        labelRepository.create(label)
        return Result.success(label)
    }
}
