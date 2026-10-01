package com.expensetracker.wallet.domain.usecase.automation

import com.expensetracker.wallet.core.common.newId
import com.expensetracker.wallet.domain.model.Account
import com.expensetracker.wallet.domain.model.AccountType
import com.expensetracker.wallet.domain.repository.AccountRepository
import com.expensetracker.wallet.domain.usecase.automation.parser.AccountHint
import java.util.Locale
import javax.inject.Inject

sealed class AccountResolution {
    data class Resolved(val accountId: String) : AccountResolution()

    /** More than one candidate account matched (or, for `BANK`, none did) — the caller can't
     * safely guess, so this always forces the overall parse down to `ParseConfidence.LOW`. */
    object Ambiguous : AccountResolution()
}

/**
 * plan.md §32 — turns an [AccountHint] into a real account id. `CASH` and the named MFS wallets
 * are singular, well-known concepts safe to auto-create the first time they're seen (mirroring
 * `ImportTransactionsUseCase`'s "create the missing account" policy from Phase 12); `BANK` is
 * deliberately never auto-created — which bank isn't knowable from a hint alone, so zero or
 * multiple `BANK`-type accounts is [AccountResolution.Ambiguous] either way.
 */
class ResolveAccountHintUseCase @Inject constructor(
    private val accountRepository: AccountRepository,
) {
    suspend operator fun invoke(hint: AccountHint, accounts: List<Account>): AccountResolution = when (hint) {
        AccountHint.CASH -> resolveByType(accounts, AccountType.CASH, "Cash")
        AccountHint.BKASH -> resolveByName(accounts, "bkash", "bKash")
        AccountHint.NAGAD -> resolveByName(accounts, "nagad", "Nagad")
        AccountHint.ROCKET -> resolveByName(accounts, "rocket", "Rocket")
        AccountHint.BANK -> {
            val matches = accounts.filter { it.type == AccountType.BANK }
            if (matches.size == 1) AccountResolution.Resolved(matches.first().id) else AccountResolution.Ambiguous
        }
    }

    private suspend fun resolveByType(accounts: List<Account>, type: AccountType, createName: String): AccountResolution {
        val matches = accounts.filter { it.type == type }
        return when {
            matches.size == 1 -> AccountResolution.Resolved(matches.first().id)
            matches.isEmpty() -> AccountResolution.Resolved(createAccount(createName, type))
            else -> AccountResolution.Ambiguous
        }
    }

    private suspend fun resolveByName(accounts: List<Account>, nameContains: String, createName: String): AccountResolution {
        val matches = accounts.filter { it.name.lowercase(Locale.US).contains(nameContains) }
        return when {
            matches.size == 1 -> AccountResolution.Resolved(matches.first().id)
            matches.isEmpty() -> AccountResolution.Resolved(createAccount(createName, AccountType.MOBILE_WALLET))
            else -> AccountResolution.Ambiguous
        }
    }

    private suspend fun createAccount(name: String, type: AccountType): String {
        val now = System.currentTimeMillis()
        val account = Account(
            id = newId(),
            name = name,
            type = type,
            institutionId = null,
            currency = "BDT",
            openingBalanceMinor = 0L,
            isArchived = false,
            createdAt = now,
            updatedAt = now,
        )
        accountRepository.create(account)
        return account.id
    }
}
