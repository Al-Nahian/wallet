package com.expensetracker.wallet.feature.accounts

/** Sub-routes pushed on top of the Accounts tab — each renders its own back-button Scaffold
 * rather than sharing the top-level WalletTopBar/bottom nav (see WalletNavHost). */
object AccountRoutes {
    const val ACCOUNT_ID_ARG = "accountId"
    const val CREATE = "accounts/create"
    const val DETAIL_PATTERN = "accounts/{$ACCOUNT_ID_ARG}"
    const val EDIT_PATTERN = "accounts/{$ACCOUNT_ID_ARG}/edit"

    fun detail(accountId: String) = "accounts/$accountId"
    fun edit(accountId: String) = "accounts/$accountId/edit"
}
