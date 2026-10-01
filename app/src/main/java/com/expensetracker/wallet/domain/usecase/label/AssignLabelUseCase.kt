package com.expensetracker.wallet.domain.usecase.label

import com.expensetracker.wallet.domain.repository.LabelRepository
import javax.inject.Inject

/** plan.md §70 — replaces a transaction's full label set (many-to-many) in one call, the shape
 * the transaction form's multi-select picker needs for both create and edit. */
class AssignLabelUseCase @Inject constructor(
    private val labelRepository: LabelRepository,
) {
    suspend operator fun invoke(transactionId: String, labelIds: Set<String>): Result<Unit> {
        labelRepository.setLabelsForTransaction(transactionId, labelIds.toList())
        return Result.success(Unit)
    }
}
