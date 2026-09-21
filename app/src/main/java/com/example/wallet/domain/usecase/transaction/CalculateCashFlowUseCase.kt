package com.example.wallet.domain.usecase.transaction

import com.example.wallet.domain.model.TransactionType
import com.example.wallet.domain.repository.TransactionRepository
import javax.inject.Inject

/**
 * plan.md §24/§26 rule 1: `Income - Expenses = Net Cash Flow`, transfers excluded. No
 * `TRANSFER` type exists until Phase 6, so today's result is already correct by construction —
 * written for real now (not a stub) because Phase 6 and Reports (Phase 9) both need it.
 */
class CalculateCashFlowUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
) {
    suspend operator fun invoke(startInclusive: Long, endInclusive: Long): Long {
        val income = transactionRepository.sumByTypeInRange(TransactionType.INCOME, startInclusive, endInclusive)
        val expense = transactionRepository.sumByTypeInRange(TransactionType.EXPENSE, startInclusive, endInclusive)
        return income - expense
    }
}
