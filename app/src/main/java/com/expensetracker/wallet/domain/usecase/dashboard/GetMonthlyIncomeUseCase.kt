package com.expensetracker.wallet.domain.usecase.dashboard

import com.expensetracker.wallet.domain.model.TransactionType
import com.expensetracker.wallet.domain.repository.TransactionRepository
import javax.inject.Inject

/** plan.md §20/§25 — "How much did I earn?" within [startInclusive, endInclusive]. */
class GetMonthlyIncomeUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
) {
    suspend operator fun invoke(startInclusive: Long, endInclusive: Long): Long =
        transactionRepository.sumByTypeInRange(TransactionType.INCOME, startInclusive, endInclusive)
}
