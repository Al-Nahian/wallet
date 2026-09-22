package com.example.wallet.domain.usecase.label

/** plan.md §56 — friendly messages, never a raw exception, ever reach the UI. */
sealed class LabelError(val userMessage: String) {
    data object NameRequired : LabelError("Enter a label name.")
    data object LabelNotFound : LabelError("We couldn't find this label. It may have been removed.")
}

class LabelValidationException(val error: LabelError) : Exception(error.userMessage)
