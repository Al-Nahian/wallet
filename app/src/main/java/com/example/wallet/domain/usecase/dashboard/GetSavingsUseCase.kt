package com.example.wallet.domain.usecase.dashboard

import javax.inject.Inject

/** plan.md §20/§25 — income minus expenses for the same period. Pure function, no DB access. */
class GetSavingsUseCase @Inject constructor() {
    operator fun invoke(incomeMinor: Long, expenseMinor: Long): Long = incomeMinor - expenseMinor
}
