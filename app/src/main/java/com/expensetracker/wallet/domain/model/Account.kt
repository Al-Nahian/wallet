package com.expensetracker.wallet.domain.model

/**
 * Domain model mirrors the entity 1:1 for now (both use Long minor units and epoch-millis
 * timestamps) — no BigDecimal/Instant conversion layer until a real need for one shows up.
 * `balanceMinor` is computed by `CalculateBalanceUseCase` (Phase 3), never stored.
 */
data class Account(
    val id: String,
    val name: String,
    val type: AccountType,
    val institutionId: String?,
    val currency: String,
    val openingBalanceMinor: Long,
    val isArchived: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
)
