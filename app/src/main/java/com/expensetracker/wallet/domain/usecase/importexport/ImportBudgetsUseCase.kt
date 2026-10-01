package com.expensetracker.wallet.domain.usecase.importexport

import com.expensetracker.wallet.core.common.newId
import com.expensetracker.wallet.domain.model.Budget
import com.expensetracker.wallet.domain.model.BudgetPeriod
import com.expensetracker.wallet.domain.repository.BudgetRepository
import javax.inject.Inject

/** Commits the accepted, error-free, non-duplicate rows from [parseBudgetRows]. Category limits
 * aren't part of the CSV, so every imported budget starts with none — same as the export side. */
class ImportBudgetsUseCase @Inject constructor(
    private val budgetRepository: BudgetRepository,
) {
    suspend operator fun invoke(rows: List<ParsedBudgetRow>): Int {
        val committable = rows.filter { it.accepted && it.errors.isEmpty() }
        if (committable.isEmpty()) return 0
        val now = System.currentTimeMillis()

        committable.forEach { row ->
            budgetRepository.create(
                Budget(
                    id = newId(),
                    name = row.name!!.trim(),
                    period = row.period ?: BudgetPeriod.MONTHLY,
                    startDate = row.startDate!!,
                    endDate = row.endDate!!,
                    amountMinor = row.amountMinor!!,
                    currency = row.currency,
                    createdAt = now,
                    updatedAt = now,
                ),
                categoryLimits = emptyList(),
            )
        }
        return committable.size
    }
}
