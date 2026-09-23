package com.example.wallet.domain.usecase.importexport

import com.example.wallet.core.common.parseMoneyToMinorUnits
import com.example.wallet.domain.model.Transaction
import com.example.wallet.domain.model.TransactionType
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

/** Common header spellings, for auto-detecting a [ImportColumn] mapping from a CSV's header row. */
private val HEADER_ALIASES: Map<ImportColumn, List<String>> = mapOf(
    ImportColumn.DATE to listOf("date", "transaction date", "txn date"),
    ImportColumn.AMOUNT to listOf("amount", "value"),
    ImportColumn.TYPE to listOf("type", "transaction type"),
    ImportColumn.ACCOUNT to listOf("account", "account name"),
    ImportColumn.CATEGORY to listOf("category"),
    ImportColumn.PAYEE to listOf("payee", "merchant", "description"),
    ImportColumn.NOTE to listOf("note", "notes", "memo"),
    ImportColumn.CURRENCY to listOf("currency", "ccy"),
)

private val DATE_FORMATS = listOf(
    "yyyy-MM-dd",
    "yyyy/MM/dd",
    "MM/dd/yyyy",
    "dd/MM/yyyy",
    "MM-dd-yyyy",
)

fun detectColumnMapping(headers: List<String>): Map<ImportColumn, Int?> =
    ImportColumn.entries.associateWith { column ->
        val aliases = HEADER_ALIASES.getValue(column)
        headers.indexOfFirst { header -> header.trim().lowercase(Locale.US) in aliases }
            .takeIf { it >= 0 }
    }

private fun parseDate(raw: String): Long? {
    val trimmed = raw.trim()
    if (trimmed.isEmpty()) return null
    for (pattern in DATE_FORMATS) {
        val format = SimpleDateFormat(pattern, Locale.US).apply { isLenient = false }
        runCatching { format.parse(trimmed) }.getOrNull()?.let { return it.time }
    }
    return null
}

private fun parseType(raw: String): TransactionType? =
    runCatching { TransactionType.valueOf(raw.trim().uppercase(Locale.US)) }.getOrNull()

/**
 * Parses+validates the raw CSV body rows into [ParsedImportRow]s per the user's confirmed
 * [ImportColumn] mapping (plans/12-import-export.md's Validation + Deduplication pipeline steps).
 * Account/category names are resolved case-insensitively against what already exists; an unknown
 * account is *not* a validation error (the import use case creates it), an unknown category is
 * left null (imported as uncategorized) rather than auto-creating category hierarchy.
 */
class MapCsvRowsUseCase @Inject constructor() {
    operator fun invoke(
        bodyRows: List<List<String>>,
        mapping: Map<ImportColumn, Int?>,
        existingCategoryNames: Set<String>,
        existingTransactions: List<Transaction>,
        accountNameById: Map<String, String>,
        defaultCurrency: String,
    ): List<ParsedImportRow> {
        val categoryNamesLower = existingCategoryNames.map { it.trim().lowercase(Locale.US) }.toSet()

        fun cell(row: List<String>, column: ImportColumn): String? {
            val index = mapping[column] ?: return null
            return row.getOrNull(index)?.trim()?.takeIf { it.isNotEmpty() }
        }

        return bodyRows.mapIndexed { index, row ->
            val errors = mutableListOf<String>()

            val dateRaw = cell(row, ImportColumn.DATE)
            val dateMillis = dateRaw?.let(::parseDate)
            if (dateRaw == null) errors.add("Missing date") else if (dateMillis == null) errors.add("Unrecognized date format")

            val amountRaw = cell(row, ImportColumn.AMOUNT)
            val amountMinor = amountRaw?.let(::parseMoneyToMinorUnits)
            if (amountRaw == null) errors.add("Missing amount") else if (amountMinor == null) errors.add("Invalid amount")

            val typeRaw = cell(row, ImportColumn.TYPE)
            val type = typeRaw?.let(::parseType)
            if (typeRaw == null) errors.add("Missing type") else if (type == null) {
                errors.add("Unknown type (expected EXPENSE, INCOME, TRANSFER, REFUND, or ADJUSTMENT)")
            }

            val accountName = cell(row, ImportColumn.ACCOUNT)
            if (accountName == null) errors.add("Missing account")

            val categoryNameRaw = cell(row, ImportColumn.CATEGORY)
            val categoryName = categoryNameRaw?.takeIf { it.lowercase(Locale.US) in categoryNamesLower }

            val payee = cell(row, ImportColumn.PAYEE)
            val note = cell(row, ImportColumn.NOTE)
            val currency = cell(row, ImportColumn.CURRENCY)?.uppercase(Locale.US) ?: defaultCurrency

            val isDuplicate = dateMillis != null && amountMinor != null && accountName != null &&
                existingTransactions.any { existing ->
                    existing.date == dateMillis &&
                        existing.amountMinor == amountMinor &&
                        accountNameById[existing.accountId]?.lowercase(Locale.US) == accountName.lowercase(Locale.US) &&
                        existing.payee?.trim()?.lowercase(Locale.US) == payee?.lowercase(Locale.US)
                }

            ParsedImportRow(
                rowIndex = index,
                dateMillis = dateMillis,
                amountMinor = amountMinor,
                type = type,
                accountName = accountName,
                categoryName = categoryName,
                payee = payee,
                note = note,
                currency = currency,
                errors = errors,
                isDuplicate = isDuplicate,
                accepted = errors.isEmpty() && !isDuplicate,
            )
        }
    }
}
