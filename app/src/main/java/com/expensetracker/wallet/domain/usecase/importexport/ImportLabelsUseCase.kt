package com.expensetracker.wallet.domain.usecase.importexport

import com.expensetracker.wallet.core.common.newId
import com.expensetracker.wallet.domain.model.Label
import com.expensetracker.wallet.domain.repository.LabelRepository
import javax.inject.Inject

/** Commits the accepted, error-free, non-duplicate rows from [parseLabelRows]. */
class ImportLabelsUseCase @Inject constructor(
    private val labelRepository: LabelRepository,
) {
    suspend operator fun invoke(rows: List<ParsedLabelRow>): Int {
        val committable = rows.filter { it.accepted && it.errors.isEmpty() }
        if (committable.isEmpty()) return 0
        val now = System.currentTimeMillis()

        committable.forEach { row ->
            labelRepository.create(Label(id = newId(), name = row.name!!.trim(), color = row.color, createdAt = now))
        }
        return committable.size
    }
}
