package com.expensetracker.wallet.domain.usecase.template

/** plan.md §56 — friendly messages, never a raw exception, ever reach the UI. */
sealed class TemplateError(val userMessage: String) {
    data object NameRequired : TemplateError("Give this template a name.")
    data object AccountRequired : TemplateError("Select an account for this template.")
    data object CategoryRequired : TemplateError("Select a category for this template.")
    data object LabelRequired : TemplateError("Select a label for this template.")
    data object TemplateNotFound : TemplateError("We couldn't find this template. It may have been removed.")
}

class TemplateValidationException(val error: TemplateError) : Exception(error.userMessage)
