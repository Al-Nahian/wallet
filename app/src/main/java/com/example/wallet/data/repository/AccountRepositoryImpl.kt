package com.example.wallet.data.repository

import com.example.wallet.data.local.dao.AccountDao
import com.example.wallet.domain.model.Account
import com.example.wallet.domain.repository.AccountRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AccountRepositoryImpl @Inject constructor(
    private val accountDao: AccountDao,
) : AccountRepository {

    override fun observeActiveAccounts(): Flow<List<Account>> =
        accountDao.observeActive().map { entities -> entities.map { it.toDomain() } }

    override fun observeAllAccounts(): Flow<List<Account>> =
        accountDao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override fun observeAccount(id: String): Flow<Account?> =
        accountDao.observeById(id).map { it?.toDomain() }

    override suspend fun getAccount(id: String): Account? = accountDao.getById(id)?.toDomain()

    override suspend fun create(account: Account) = accountDao.insert(account.toEntity())

    override suspend fun update(account: Account) = accountDao.update(account.toEntity())
}
