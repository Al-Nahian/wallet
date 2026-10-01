package com.expensetracker.wallet.domain.usecase.reports

import com.expensetracker.wallet.domain.model.Account
import com.expensetracker.wallet.domain.model.TransactionType
import com.expensetracker.wallet.domain.repository.TransactionRepository
import com.expensetracker.wallet.domain.usecase.account.CalculateBalanceUseCase
import javax.inject.Inject

data class AccountReport(
    val accountId: String,
    val accountName: String,
    val currency: String,
    val currentBalanceMinor: Long,
    val inflowMinor: Long,
    val outflowMinor: Long,
    val netMinor: Long,
)

/** plan.md §24/§25 `getAccountBalance()` — an account's current balance (§11, the same ledger
 * sum as the dashboard/account screens) alongside its inflow/outflow *within the report's
 * selected range*, both DB-aggregated (§54). */
class GetAccountReportUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val calculateBalance: CalculateBalanceUseCase,
) {
    suspend operator fun invoke(account: Account, startInclusive: Long, endInclusive: Long): AccountReport {
        val balance = calculateBalance(account)
        val inflow = transactionRepository.sumByAccountAndTypeInRange(
            account.id, TransactionType.INCOME, startInclusive, endInclusive,
        )
        val outflow = transactionRepository.sumByAccountAndTypeInRange(
            account.id, TransactionType.EXPENSE, startInclusive, endInclusive,
        )
        return AccountReport(
            accountId = account.id,
            accountName = account.name,
            currency = account.currency,
            currentBalanceMinor = balance,
            inflowMinor = inflow,
            outflowMinor = outflow,
            netMinor = inflow - outflow,
        )
    }
}
