package com.expensetracker.wallet.domain.usecase.dashboard

import javax.inject.Inject

/** plan.md §20/§25 — savings as a percentage of income. Zero (never a divide-by-zero crash)
 * when there's no income yet to divide by. Pure function, no DB access. */
class GetSavingsRateUseCase @Inject constructor() {
    operator fun invoke(savingsMinor: Long, incomeMinor: Long): Double =
        if (incomeMinor <= 0L) 0.0 else savingsMinor * 100.0 / incomeMinor
}
