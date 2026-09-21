package com.example.wallet.domain.usecase.account

/**
 * User-facing account validation/lookup errors (plan.md §56 — friendly messages, not raw
 * exceptions, ever reach the UI). Thrown only as [AccountValidationException], never bubbled
 * up as a generic exception.
 */
sealed class AccountError(val userMessage: String) {
    data object NameRequired : AccountError("Please enter an account name.")
    data object InvalidOpeningBalance : AccountError("Enter a valid opening balance, e.g. 1000.00.")
    data object CurrencyRequired : AccountError("Please select a currency.")
    data object AccountNotFound : AccountError("We couldn't find this account. It may have been removed.")
}

class AccountValidationException(val error: AccountError) : Exception(error.userMessage)
