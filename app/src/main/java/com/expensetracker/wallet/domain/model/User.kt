package com.expensetracker.wallet.domain.model

/**
 * Local mirror of the eventual Supabase `public.profiles` row (plan.md §84). No row exists
 * until Phase 16 makes sign-in real — absence of a row *is* guest mode (plan.md §85), not a
 * separate "isSignedIn" flag to keep in sync.
 */
data class User(
    val id: String,
    val displayName: String,
    val avatarUrl: String?,
    val createdAt: Long,
    val updatedAt: Long,
)
