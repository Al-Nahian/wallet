package com.example.wallet.domain.usecase.account

import com.example.wallet.domain.model.Account
import com.example.wallet.domain.model.TransactionType
import com.example.wallet.domain.repository.TransactionRepository
import javax.inject.Inject

/**
 * plan.md §11: balance = opening balance + ledger movements, never a stored-and-trusted column.
 * Sums via database aggregation (§54 — never load the full transaction list to sum in Kotlin);
 * soft-deleted transactions are excluded at the query level (§44).
 *
 * Transfers (Phase 6) aren't summed here yet — there's no `TRANSFER`-type transaction possible
 * until that phase adds it, so this is deliberately income/expense-only for now, not a gap.
 */
class CalculateBalanceUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
) {
    suspend operator fun invoke(account: Account): Long {
        val income = transactionRepository.sumByAccountAndType(account.id, TransactionType.INCOME)
        val expense = transactionRepository.sumByAccountAndType(account.id, TransactionType.EXPENSE)
        return account.openingBalanceMinor + income - expense
    }
}
