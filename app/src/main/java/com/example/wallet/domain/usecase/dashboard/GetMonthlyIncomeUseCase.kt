package com.example.wallet.domain.usecase.dashboard

import com.example.wallet.domain.model.TransactionType
import com.example.wallet.domain.repository.TransactionRepository
import javax.inject.Inject

/** plan.md §20/§25 — "How much did I earn?" within [startInclusive, endInclusive]. */
class GetMonthlyIncomeUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
) {
    suspend operator fun invoke(startInclusive: Long, endInclusive: Long): Long =
        transactionRepository.sumByTypeInRange(TransactionType.INCOME, startInclusive, endInclusive)
}
