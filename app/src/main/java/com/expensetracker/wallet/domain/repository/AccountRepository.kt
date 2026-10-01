package com.expensetracker.wallet.domain.repository

import com.expensetracker.wallet.domain.model.Account
import kotlinx.coroutines.flow.Flow

/** Implemented in Phase 3. */
interface AccountRepository {
    fun observeActiveAccounts(): Flow<List<Account>>
    fun observeAllAccounts(): Flow<List<Account>>
    fun observeAccount(id: String): Flow<Account?>
    suspend fun getAccount(id: String): Account?
    suspend fun create(account: Account)
    suspend fun update(account: Account)
}
