package com.example.wallet.data.repository

import com.example.wallet.data.local.dao.UserDao
import com.example.wallet.domain.model.User
import com.example.wallet.domain.repository.UserRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
) : UserRepository {

    override fun observeCurrentUser(): Flow<User?> =
        userDao.observeCurrentUser().map { it?.toDomain() }
}
