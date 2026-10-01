package com.expensetracker.wallet.domain.usecase.account

import com.expensetracker.wallet.domain.model.Account
import com.expensetracker.wallet.domain.model.TransactionType
import com.expensetracker.wallet.domain.repository.TransactionRepository
import javax.inject.Inject

/**
 * plan.md §11: balance = opening balance + ledger movements, never a stored-and-trusted column.
 * Sums via database aggregation (§54 — never load the full transaction list to sum in Kotlin);
 * soft-deleted transactions are excluded at the query level (§44).
 *
 * Transfers (§22) are included via [TransactionType.TRANSFER]'s own signed sum:
 * `CreateTransferUseCase` stores the outgoing leg's `amountMinor` negated and the incoming leg
 * positive, so `SUM(amountMinor)` for a given account already nets to the correct delta — no
 * separate direction column needed, and transfers never touch the income/expense sums above
 * (§26 rule 1).
 */
class CalculateBalanceUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
) {
    suspend operator fun invoke(account: Account): Long {
        val income = transactionRepository.sumByAccountAndType(account.id, TransactionType.INCOME)
        val expense = transactionRepository.sumByAccountAndType(account.id, TransactionType.EXPENSE)
        val transferNet = transactionRepository.sumByAccountAndType(account.id, TransactionType.TRANSFER)
        return account.openingBalanceMinor + income - expense + transferNet
    }
}
