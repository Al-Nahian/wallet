package com.expensetracker.wallet.domain.repository

import com.expensetracker.wallet.domain.model.User
import kotlinx.coroutines.flow.Flow

/** plan.md §84/§85. Real sign-in (and therefore ever writing a row) is Phase 16's job. */
interface UserRepository {
    fun observeCurrentUser(): Flow<User?>

    suspend fun signInWithGoogle(): Result<Unit>
    suspend fun signInWithAzure(): Result<Unit>
    suspend fun signInWithEmail(email: String, password: String): Result<Unit>
    suspend fun signUpWithEmail(email: String, password: String): Result<Unit>
    suspend fun signOut(): Result<Unit>
}
