package com.example.wallet.domain.repository

import com.example.wallet.domain.model.User
import kotlinx.coroutines.flow.Flow

/** plan.md §84/§85. Real sign-in (and therefore ever writing a row) is Phase 16's job. */
interface UserRepository {
    fun observeCurrentUser(): Flow<User?>
}
