package com.example.wallet.feature.accounts

import com.example.wallet.domain.model.AccountType

data class AccountUi(
    val id: String,
    val name: String,
    val type: AccountType,
    val currency: String,
    val balanceMinor: Long,
)
