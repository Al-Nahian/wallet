package com.expensetracker.wallet.data.repository

import com.expensetracker.wallet.data.local.dao.UserDao
import com.expensetracker.wallet.data.local.entity.UserEntity
import com.expensetracker.wallet.domain.model.User
import com.expensetracker.wallet.domain.repository.UserRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Azure
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.postgrest.postgrest
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val supabaseClient: SupabaseClient,
) : UserRepository {

    override fun observeCurrentUser(): Flow<User?> =
        userDao.observeCurrentUser().map { it?.toDomain() }

    override suspend fun signInWithGoogle(): Result<Unit> = runCatching {
        supabaseClient.auth.signInWith(Google)
        persistSignedInUser()
    }

    override suspend fun signInWithAzure(): Result<Unit> = runCatching {
        supabaseClient.auth.signInWith(Azure)
        persistSignedInUser()
    }

    override suspend fun signInWithEmail(email: String, password: String): Result<Unit> = runCatching {
        supabaseClient.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
        persistSignedInUser()
    }

    override suspend fun signUpWithEmail(email: String, password: String): Result<Unit> = runCatching {
        supabaseClient.auth.signUpWith(Email) {
            this.email = email
            this.password = password
        }
        persistSignedInUser()
    }

    override suspend fun signOut(): Result<Unit> = runCatching {
        supabaseClient.auth.signOut()
        userDao.clear()
    }

    // §85 is "no local row = guest mode" — this is the one place that ever flips it. It also
    // makes the single write this phase does to the new schema: a `public.profiles` upsert.
    private suspend fun persistSignedInUser() {
        val userInfo = requireNotNull(supabaseClient.auth.currentUserOrNull()) {
            "Sign-in reported success but no session user was returned"
        }
        val now = System.currentTimeMillis()
        val displayName = userInfo.userMetadata
            ?.get("full_name")?.jsonPrimitive?.contentOrNull
            ?: userInfo.userMetadata?.get("name")?.jsonPrimitive?.contentOrNull
            ?: userInfo.email?.substringBefore("@")
            ?: "Wallet user"
        val avatarUrl = userInfo.userMetadata
            ?.get("avatar_url")?.jsonPrimitive?.contentOrNull
            ?: userInfo.userMetadata?.get("picture")?.jsonPrimitive?.contentOrNull

        userDao.upsert(
            UserEntity(
                id = userInfo.id,
                displayName = displayName,
                avatarUrl = avatarUrl,
                createdAt = userInfo.createdAt?.toEpochMilliseconds() ?: now,
                updatedAt = now,
            ),
        )

        supabaseClient.postgrest.from("profiles").upsert(
            ProfileRow(
                id = userInfo.id,
                displayName = displayName,
                avatarUrl = avatarUrl,
                createdAt = userInfo.createdAt?.toEpochMilliseconds() ?: now,
                updatedAt = now,
            ),
        )
    }
}

@Serializable
private data class ProfileRow(
    val id: String,
    @kotlinx.serialization.SerialName("display_name") val displayName: String,
    @kotlinx.serialization.SerialName("avatar_url") val avatarUrl: String?,
    @kotlinx.serialization.SerialName("created_at") val createdAt: Long,
    @kotlinx.serialization.SerialName("updated_at") val updatedAt: Long,
)
