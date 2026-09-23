package com.example.wallet.domain.usecase.importexport

import com.example.wallet.domain.model.TransactionType

/** plans/12-import-export.md — the fixed set of fields a CSV column can be mapped to. */
enum class ImportColumn(val label: String) {
    DATE("Date"),
    AMOUNT("Amount"),
    TYPE("Type"),
    ACCOUNT("Account"),
    CATEGORY("Category"),
    PAYEE("Payee"),
    NOTE("Note"),
    CURRENCY("Currency"),
}

/**
 * One parsed+validated CSV data row, ready for the preview screen. `errors` being non-empty
 * blocks commit for that row regardless of `accepted` (§36 — "never import without preview",
 * a row-level error is shown, not a hard crash). `accepted` is the user's per-row toggle in the
 * preview, defaulted to true unless the row has errors or looks like a duplicate.
 */
data class ParsedImportRow(
    val rowIndex: Int,
    val dateMillis: Long?,
    val amountMinor: Long?,
    val type: TransactionType?,
    val accountName: String?,
    val categoryName: String?,
    val payee: String?,
    val note: String?,
    val currency: String?,
    val errors: List<String>,
    val isDuplicate: Boolean,
    val accepted: Boolean,
)
