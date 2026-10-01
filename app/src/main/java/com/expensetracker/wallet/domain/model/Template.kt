package com.expensetracker.wallet.domain.model

/** A saved shortcut for quickly starting a recurring transaction (e.g. "Lunch": account Wallet,
 * category Food, label Food) — applied from the transaction form to pre-fill its account,
 * category, label, and optional payee/place in one tap, leaving only amount and type for the
 * user to fill in themselves. */
data class Template(
    val id: String,
    val name: String,
    val accountId: String,
    val categoryId: String,
    val labelId: String,
    val payee: String?,
    val place: String?,
    val createdAt: Long,
)
