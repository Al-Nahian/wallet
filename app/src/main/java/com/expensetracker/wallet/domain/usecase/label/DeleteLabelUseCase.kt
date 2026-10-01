package com.expensetracker.wallet.domain.usecase.label

import com.expensetracker.wallet.domain.repository.LabelRepository
import javax.inject.Inject

/** No in-use block needed (unlike categories): `transaction_labels` cascade-deletes cleanly,
 * removing only the tag, never a transaction or split. */
class DeleteLabelUseCase @Inject constructor(
    private val labelRepository: LabelRepository,
) {
    suspend operator fun invoke(labelId: String): Result<Unit> {
        labelRepository.getLabel(labelId)
            ?: return Result.failure(LabelValidationException(LabelError.LabelNotFound))

        labelRepository.delete(labelId)
        return Result.success(Unit)
    }
}
