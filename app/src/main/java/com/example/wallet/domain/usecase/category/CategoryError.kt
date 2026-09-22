package com.example.wallet.domain.usecase.category

/** plan.md §56 — friendly messages, never a raw exception, ever reach the UI. */
sealed class CategoryError(val userMessage: String) {
    data object NameRequired : CategoryError("Enter a category name.")
    data object GroupNotFound : CategoryError("We couldn't find that category group.")
    data object CategoryNotFound : CategoryError("We couldn't find this category. It may have been removed.")
    data object SystemCategoryImmutable : CategoryError("Default categories can't be renamed or deleted.")
    data object CategoryInUse : CategoryError("This category is used by existing transactions and can't be deleted.")
}

class CategoryValidationException(val error: CategoryError) : Exception(error.userMessage)
