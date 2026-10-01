package com.expensetracker.wallet.feature.accounts

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.RequestQuote
import androidx.compose.material.icons.filled.Savings
import androidx.compose.ui.graphics.vector.ImageVector
import com.expensetracker.wallet.domain.model.AccountType

fun AccountType.label(): String = when (this) {
    AccountType.BANK -> "Bank"
    AccountType.CASH -> "Cash"
    AccountType.CREDIT_CARD -> "Credit Card"
    AccountType.MOBILE_WALLET -> "Mobile Wallet"
    AccountType.SAVINGS -> "Savings"
    AccountType.INVESTMENT -> "Investment"
    AccountType.LOAN -> "Loan"
    AccountType.OTHER -> "Other"
}

fun AccountType.icon(): ImageVector = when (this) {
    AccountType.BANK -> Icons.Filled.AccountBalance
    AccountType.CASH -> Icons.Filled.Payments
    AccountType.CREDIT_CARD -> Icons.Filled.CreditCard
    AccountType.MOBILE_WALLET -> Icons.Filled.AccountBalanceWallet
    AccountType.SAVINGS -> Icons.Filled.Savings
    AccountType.INVESTMENT -> Icons.AutoMirrored.Filled.TrendingUp
    AccountType.LOAN -> Icons.Filled.RequestQuote
    AccountType.OTHER -> Icons.Filled.Category
}
