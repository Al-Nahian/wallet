package com.example.wallet.domain.usecase.transaction

/** plan.md §56 — friendly messages, never a raw exception, ever reach the UI. */
sealed class TransactionError(val userMessage: String) {
    data object InvalidAmount : TransactionError("Enter a valid amount greater than zero.")
    data object AccountRequired : TransactionError("Please select an account.")
    data object AccountNotFound : TransactionError("We couldn't find this account. It may have been removed.")
    data object AccountArchived : TransactionError("This account is archived. Choose another account.")
    data object TransactionNotFound : TransactionError("We couldn't find this transaction. It may have been removed.")
}

class TransactionValidationException(val error: TransactionError) : Exception(error.userMessage)
