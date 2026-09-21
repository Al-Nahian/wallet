package com.example.wallet.domain.usecase.account

import com.example.wallet.domain.model.Account
import javax.inject.Inject

/**
 * plan.md §11: balance = opening balance + ledger movements, never a stored-and-trusted column.
 * Phase 5 (once `TransactionRepository` has a real implementation) extends this to sum the
 * account's income/expense/transfer transactions; until then there are none, so balance is
 * exactly the opening balance.
 */
class CalculateBalanceUseCase @Inject constructor() {
    operator fun invoke(account: Account): Long = account.openingBalanceMinor
}
