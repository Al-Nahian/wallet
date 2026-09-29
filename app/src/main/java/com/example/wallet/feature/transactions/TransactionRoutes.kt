package com.example.wallet.feature.transactions

/** Sub-routes pushed on top of the Transactions tab, each with its own back-button Scaffold
 * (same pattern as plans/03-accounts.md's AccountRoutes). */
object TransactionRoutes {
    const val TRANSACTION_ID_ARG = "transactionId"
    const val TEMPLATE_ID_ARG = "templateId"
    const val CREATE = "transactions/create"
    // Registered as one composable with an optional query arg (defaulted to null) rather than
    // a second route for CREATE — navigating to the bare CREATE literal still matches this
    // pattern with templateId absent, so every existing "add transaction" call site is unaffected.
    const val CREATE_PATTERN = "transactions/create?$TEMPLATE_ID_ARG={$TEMPLATE_ID_ARG}"
    const val EDIT_PATTERN = "transactions/{$TRANSACTION_ID_ARG}/edit"

    fun edit(transactionId: String) = "transactions/$transactionId/edit"
    fun createFromTemplate(templateId: String) = "transactions/create?$TEMPLATE_ID_ARG=$templateId"
}
