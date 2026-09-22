package com.example.wallet.domain.usecase.dashboard

import javax.inject.Inject

/** plan.md §20/§25 — this month's expenses spread evenly across the days elapsed so far. Pure
 * function, no DB access. */
class GetAverageDailySpendUseCase @Inject constructor() {
    operator fun invoke(monthlyExpenseMinor: Long, dayOfMonth: Int): Long =
        if (dayOfMonth <= 0) 0L else monthlyExpenseMinor / dayOfMonth
}
