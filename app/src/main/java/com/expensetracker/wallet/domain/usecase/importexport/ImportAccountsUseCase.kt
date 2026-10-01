package com.expensetracker.wallet.domain.usecase.importexport

import com.expensetracker.wallet.core.common.newId
import com.expensetracker.wallet.domain.model.Account
import com.expensetracker.wallet.domain.repository.AccountRepository
import com.expensetracker.wallet.domain.repository.InstitutionRepository
import javax.inject.Inject

/** Commits the accepted, error-free, non-duplicate rows from [parseAccountRows]. */
class ImportAccountsUseCase @Inject constructor(
    private val accountRepository: AccountRepository,
    private val institutionRepository: InstitutionRepository,
) {
    suspend operator fun invoke(rows: List<ParsedAccountRow>): Int {
        val committable = rows.filter { it.accepted && it.errors.isEmpty() }
        if (committable.isEmpty()) return 0
        val now = System.currentTimeMillis()

        committable.forEach { row ->
            val institutionId = row.institutionName?.takeIf { it.isNotBlank() }
                ?.let { institutionRepository.findOrCreateByName(it).id }
            accountRepository.create(
                Account(
                    id = newId(),
                    name = row.name!!.trim(),
                    type = row.type,
                    institutionId = institutionId,
                    currency = row.currency,
                    openingBalanceMinor = row.openingBalanceMinor,
                    isArchived = row.isArchived,
                    createdAt = now,
                    updatedAt = now,
                ),
            )
        }
        return committable.size
    }
}
