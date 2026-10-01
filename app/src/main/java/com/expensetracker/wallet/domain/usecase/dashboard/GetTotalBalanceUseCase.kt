package com.expensetracker.wallet.domain.usecase.dashboard

import com.expensetracker.wallet.domain.model.Account
import com.expensetracker.wallet.domain.usecase.account.CalculateBalanceUseCase
import javax.inject.Inject

/** plan.md §20/§25 — "How much money do I have?", summed across every active account. */
class GetTotalBalanceUseCase @Inject constructor(
    private val calculateBalance: CalculateBalanceUseCase,
) {
    suspend operator fun invoke(accounts: List<Account>): Long =
        accounts.sumOf { calculateBalance(it) }
}
