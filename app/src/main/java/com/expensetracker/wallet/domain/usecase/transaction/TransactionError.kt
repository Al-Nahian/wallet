package com.expensetracker.wallet.domain.usecase.transaction

/** plan.md §56 — friendly messages, never a raw exception, ever reach the UI. */
sealed class TransactionError(val userMessage: String) {
    data object InvalidAmount : TransactionError("Enter a valid amount greater than zero.")
    data object AccountRequired : TransactionError("Please select an account.")
    data object AccountNotFound : TransactionError("We couldn't find this account. It may have been removed.")
    data object AccountArchived : TransactionError("This account is archived. Choose another account.")
    data object TransactionNotFound : TransactionError("We couldn't find this transaction. It may have been removed.")

    // plan.md §22 — Transfer flow.
    data object DestinationAccountRequired : TransactionError("Please select a destination account.")
    data object DestinationAccountNotFound : TransactionError("We couldn't find the destination account. It may have been removed.")
    data object DestinationAccountArchived : TransactionError("The destination account is archived. Choose another account.")
    data object SameAccountTransfer : TransactionError("Choose two different accounts for a transfer.")

    // plan.md §13/§26 rule 3 — Split transactions.
    data object SplitSumMismatch : TransactionError("Split amounts must add up to the transaction total.")
}

class TransactionValidationException(val error: TransactionError) : Exception(error.userMessage)
