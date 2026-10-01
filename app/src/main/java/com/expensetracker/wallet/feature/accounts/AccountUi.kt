package com.expensetracker.wallet.feature.accounts

import com.expensetracker.wallet.domain.model.AccountType

data class AccountUi(
    val id: String,
    val name: String,
    val type: AccountType,
    val currency: String,
    val balanceMinor: Long,
)
