package com.example.wallet.feature.transactions

/** Sub-routes pushed on top of the Transactions tab, each with its own back-button Scaffold
 * (same pattern as plans/03-accounts.md's AccountRoutes). */
object TransactionRoutes {
    const val TRANSACTION_ID_ARG = "transactionId"
    const val CREATE = "transactions/create"
    const val EDIT_PATTERN = "transactions/{$TRANSACTION_ID_ARG}/edit"

    fun edit(transactionId: String) = "transactions/$transactionId/edit"
}
