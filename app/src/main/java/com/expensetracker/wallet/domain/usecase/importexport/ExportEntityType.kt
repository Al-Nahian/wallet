package com.expensetracker.wallet.domain.usecase.importexport

/** The five entities this app can export/import as CSV (plans/12-import-export.md §37). */
enum class ExportEntityType(val label: String, val suggestedFileName: String) {
    TRANSACTIONS("Transactions", "wallet_transactions.csv"),
    ACCOUNTS("Accounts", "wallet_accounts.csv"),
    BUDGETS("Budgets", "wallet_budgets.csv"),
    CATEGORIES("Categories", "wallet_categories.csv"),
    LABELS("Labels", "wallet_labels.csv"),
    TEMPLATES("Templates", "wallet_templates.csv"),
}
