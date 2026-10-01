package com.expensetracker.wallet.domain.usecase.dashboard

import com.expensetracker.wallet.domain.model.TransactionType
import com.expensetracker.wallet.domain.repository.TransactionRepository
import javax.inject.Inject

/** plan.md §20/§25 — "How much did I spend?" within [startInclusive, endInclusive]. Transfers
 * are excluded by construction: only `EXPENSE`-type rows are summed (§26 rule 1). */
class GetMonthlyExpensesUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
) {
    suspend operator fun invoke(startInclusive: Long, endInclusive: Long): Long =
        transactionRepository.sumByTypeInRange(TransactionType.EXPENSE, startInclusive, endInclusive)
}
