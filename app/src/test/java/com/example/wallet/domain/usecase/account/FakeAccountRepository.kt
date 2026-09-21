package com.example.wallet.domain.usecase.account

import com.example.wallet.domain.model.Account
import com.example.wallet.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/** In-memory fake so use-case tests don't need Room/Android. */
class FakeAccountRepository : AccountRepository {
    private val accounts = MutableStateFlow<Map<String, Account>>(emptyMap())

    override fun observeActiveAccounts(): Flow<List<Account>> =
        accounts.map { it.values.filterNot { account -> account.isArchived } }

    override fun observeAllAccounts(): Flow<List<Account>> = accounts.map { it.values.toList() }

    override fun observeAccount(id: String): Flow<Account?> = accounts.map { it[id] }

    override suspend fun getAccount(id: String): Account? = accounts.value[id]

    override suspend fun create(account: Account) {
        accounts.value = accounts.value + (account.id to account)
    }

    override suspend fun update(account: Account) {
        accounts.value = accounts.value + (account.id to account)
    }
}
