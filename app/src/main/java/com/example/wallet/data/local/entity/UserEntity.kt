package com.example.wallet.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * plan.md §84/§85 — added in migration v1 -> v2 (Phase 4). Local mirror of the eventual
 * Supabase `public.profiles` row; stays empty (guest mode) until Phase 16 makes sign-in real.
 */
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val displayName: String,
    val avatarUrl: String? = null,
    val createdAt: Long,
    val updatedAt: Long,
)
