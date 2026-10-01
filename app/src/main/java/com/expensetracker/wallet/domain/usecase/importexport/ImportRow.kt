package com.expensetracker.wallet.domain.usecase.importexport

/**
 * A format-agnostic view over one parsed import row, so the preview step (and its accept/reject
 * toggle) is written once regardless of which [ExportEntityType] is being imported. Each variant
 * wraps the real typed row — [com.expensetracker.wallet.feature.importexport.ImportWizardViewModel]
 * unwraps back to the typed list (via `filterIsInstance`) when it's time to commit, since each
 * entity's commit use case needs its own typed fields, not just a display string.
 */
sealed class ImportRow {
    abstract val rowIndex: Int
    abstract val title: String
    abstract val subtitle: String
    abstract val errors: List<String>
    abstract val isDuplicate: Boolean
    abstract val accepted: Boolean
    abstract fun withAccepted(accepted: Boolean): ImportRow

    data class TransactionRow(val data: ParsedImportRow) : ImportRow() {
        override val rowIndex get() = data.rowIndex
        override val title get() = "${data.accountName ?: "?"} · ${data.type?.name ?: "?"}"
        override val subtitle get() = data.categoryName ?: data.labelName ?: data.payee ?: "(uncategorized)"
        override val errors get() = data.errors
        override val isDuplicate get() = data.isDuplicate
        override val accepted get() = data.accepted
        override fun withAccepted(accepted: Boolean) = copy(data = data.copy(accepted = accepted))
    }

    data class AccountRow(val data: ParsedAccountRow) : ImportRow() {
        override val rowIndex get() = data.rowIndex
        override val title get() = data.name ?: "?"
        override val subtitle get() = "${data.type.name} · ${data.currency}"
        override val errors get() = data.errors
        override val isDuplicate get() = data.isDuplicate
        override val accepted get() = data.accepted
        override fun withAccepted(accepted: Boolean) = copy(data = data.copy(accepted = accepted))
    }

    data class CategoryRow(val data: ParsedCategoryRow) : ImportRow() {
        override val rowIndex get() = data.rowIndex
        override val title get() = data.categoryName ?: "?"
        override val subtitle get() = "${data.groupName ?: "?"} · ${data.groupType?.name ?: "?"}"
        override val errors get() = data.errors
        override val isDuplicate get() = data.isDuplicate
        override val accepted get() = data.accepted
        override fun withAccepted(accepted: Boolean) = copy(data = data.copy(accepted = accepted))
    }

    data class LabelRow(val data: ParsedLabelRow) : ImportRow() {
        override val rowIndex get() = data.rowIndex
        override val title get() = data.name ?: "?"
        override val subtitle get() = data.color
        override val errors get() = data.errors
        override val isDuplicate get() = data.isDuplicate
        override val accepted get() = data.accepted
        override fun withAccepted(accepted: Boolean) = copy(data = data.copy(accepted = accepted))
    }

    data class BudgetRow(val data: ParsedBudgetRow) : ImportRow() {
        override val rowIndex get() = data.rowIndex
        override val title get() = data.name ?: "?"
        override val subtitle get() = "${data.period?.name ?: "?"} · ${data.currency}"
        override val errors get() = data.errors
        override val isDuplicate get() = data.isDuplicate
        override val accepted get() = data.accepted
        override fun withAccepted(accepted: Boolean) = copy(data = data.copy(accepted = accepted))
    }

    data class TemplateRow(val data: ParsedTemplateRow) : ImportRow() {
        override val rowIndex get() = data.rowIndex
        override val title get() = data.name ?: "?"
        override val subtitle get() = "${data.accountName ?: "?"} · ${data.categoryName ?: "?"} · ${data.labelName ?: "?"}"
        override val errors get() = data.errors
        override val isDuplicate get() = data.isDuplicate
        override val accepted get() = data.accepted
        override fun withAccepted(accepted: Boolean) = copy(data = data.copy(accepted = accepted))
    }
}
