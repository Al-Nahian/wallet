package com.expensetracker.wallet.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.expensetracker.wallet.domain.model.AccountType

/**
 * plan.md §11. No `user_id` yet — this app is local-only/single-user until Phase 4
 * (Notification Center & Account Shell) and Phase 16 (Backend & Auth) introduce a
 * real signed-in user to associate data with.
 *
 * `currentBalanceMinor` is never trusted as the source of truth on its own — per §11,
 * balance must be reconstructable from `openingBalanceMinor` + the transaction ledger.
 * It is not stored here at all; `CalculateBalanceUseCase` (Phase 3) computes it.
 */
@Entity(
    tableName = "accounts",
    foreignKeys = [
        ForeignKey(
            entity = InstitutionEntity::class,
            parentColumns = ["id"],
            childColumns = ["institutionId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [Index("institutionId"), Index("isArchived")],
)
data class AccountEntity(
    @PrimaryKey val id: String,
    val name: String,
    val type: AccountType,
    val institutionId: String? = null,
    val currency: String,
    val openingBalanceMinor: Long,
    val isArchived: Boolean = false,
    val createdAt: Long,
    val updatedAt: Long,
)
