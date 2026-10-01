package com.expensetracker.wallet.domain.usecase.importexport

import com.expensetracker.wallet.core.common.parseMoneyToMinorUnits
import com.expensetracker.wallet.domain.model.Category
import com.expensetracker.wallet.domain.model.CategoryGroup
import com.expensetracker.wallet.domain.model.CategoryType
import com.expensetracker.wallet.domain.model.Transaction
import com.expensetracker.wallet.domain.model.TransactionType
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

/** Common header spellings, for auto-detecting a [ImportColumn] mapping from a CSV's header row. */
private val HEADER_ALIASES: Map<ImportColumn, List<String>> = mapOf(
    ImportColumn.DATE to listOf("date", "transaction date", "txn date"),
    ImportColumn.TIME to listOf("time", "transaction time"),
    ImportColumn.AMOUNT to listOf("amount", "value"),
    ImportColumn.TYPE to listOf("type", "transaction type"),
    ImportColumn.ACCOUNT to listOf("account", "account name"),
    ImportColumn.CATEGORY to listOf("category"),
    ImportColumn.LABEL to listOf("label", "labels", "tag", "tags"),
    ImportColumn.PAYEE to listOf("payee", "merchant", "description"),
    ImportColumn.NOTE to listOf("note", "notes", "memo"),
    ImportColumn.CURRENCY to listOf("currency", "ccy"),
)

/** "January 31, 2026" (this app's own CSV export, and common spreadsheet exports) first, then a
 * handful of other common spellings. */
private val DATE_FORMATS = listOf(
    "MMMM d, yyyy",
    "yyyy-MM-dd",
    "yyyy/MM/dd",
    "MM/dd/yyyy",
    "dd/MM/yyyy",
    "MM-dd-yyyy",
)

/** "12:45 PM" / "6:30 PM", falling back to 24-hour. */
private val TIME_FORMATS = listOf("h:mm a", "hh:mm a", "HH:mm")

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

/** Returns hour-of-day (0-23) and minute, or null if [raw] doesn't match a known time format. */
private fun parseTimeOfDay(raw: String): Pair<Int, Int>? {
    val trimmed = raw.trim()
    if (trimmed.isEmpty()) return null
    for (pattern in TIME_FORMATS) {
        val format = SimpleDateFormat(pattern, Locale.US).apply { isLenient = false }
        val parsed = runCatching { format.parse(trimmed) }.getOrNull() ?: continue
        val calendar = Calendar.getInstance().apply { time = parsed }
        return calendar.get(Calendar.HOUR_OF_DAY) to calendar.get(Calendar.MINUTE)
    }
    return null
}

private fun combineDateAndTime(dateMillis: Long, timeOfDay: Pair<Int, Int>?): Long {
    if (timeOfDay == null) return dateMillis
    val (hour, minute) = timeOfDay
    return Calendar.getInstance().apply {
        timeInMillis = dateMillis
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}

private fun parseType(raw: String): TransactionType? =
    runCatching { TransactionType.valueOf(raw.trim().uppercase(Locale.US)) }.getOrNull()

/** Strips a currency code/symbol (e.g. "BDT", "$") and thousands separators so
 * `"-BDT 1,500.00"` parses the same as `"-1500.00"`; the sign is read separately since the
 * minus sign can sit before or after the currency code depending on the source app. */
private fun parseImportAmount(raw: String): Long? {
    val trimmed = raw.trim()
    if (trimmed.isEmpty()) return null
    val isNegative = trimmed.contains('-')
    val digitsOnly = trimmed.filter { it.isDigit() || it == '.' }
    if (digitsOnly.isEmpty()) return null
    val magnitude = parseMoneyToMinorUnits(digitsOnly) ?: return null
    return if (isNegative) -magnitude else magnitude
}

private val CURRENCY_CODE_REGEX = Regex("[A-Za-z]{3}")

private fun extractCurrencyCode(raw: String): String? = CURRENCY_CODE_REGEX.find(raw)?.value?.uppercase(Locale.US)

/** Strips everything but letters/digits and lowercases, so punctuation/spacing differences
 * ("Bar, cafe" vs "Bar, Cafe", "Loans, interests" vs "Loans / Interests") don't block a match. */
private fun normalizeCategoryKey(raw: String): String = raw.lowercase(Locale.US).filter { it.isLetterOrDigit() }

/**
 * Matches a CSV category's free-text name against this app's own taxonomy, ignoring
 * punctuation/case, with a light plural fallback ("Financial expenses" -> "Financial Expense"),
 * and prefers whichever candidate's group matches [desiredType] when a name collides across an
 * expense and an income group (e.g. "Child Support" exists in both).
 */
private class CategoryMatcher(categories: List<Category>, groups: List<CategoryGroup>) {
    private val groupById = groups.associateBy { it.id }
    private val index: Map<String, List<Category>> = categories.groupBy { normalizeCategoryKey(it.name) }

    fun match(rawName: String, desiredType: TransactionType): Category? {
        val key = normalizeCategoryKey(rawName)
        val candidates = index[key] ?: index[key.removeSuffix("s")] ?: return null
        if (candidates.size == 1) return candidates.first()
        val wantCategoryType = if (desiredType == TransactionType.INCOME) CategoryType.INCOME else CategoryType.EXPENSE
        return candidates.firstOrNull { groupById[it.groupId]?.type == wantCategoryType } ?: candidates.first()
    }
}

/**
 * Parses+validates the raw CSV body rows into [ParsedImportRow]s per the user's confirmed
 * [ImportColumn] mapping (plans/12-import-export.md's Validation + Deduplication pipeline steps).
 *
 * Transaction type: if a `Type` column is mapped and its value parses to a known
 * [TransactionType], that value wins (round-tripping this app's own export). Otherwise it's
 * derived — a category whose name mentions "transfer" becomes `TRANSFER` (this app has no
 * category for transfers, matching `CreateTransferUseCase`'s convention), everything else is
 * `EXPENSE` for a negative amount or `INCOME` for a positive one, which is how every common
 * expense-tracker CSV export (this app's own included) encodes the sign.
 *
 * Account names are resolved case-insensitively against what already exists; an unknown account
 * is *not* a validation error (the import use case creates it). Category names are resolved via
 * [CategoryMatcher] against this app's existing taxonomy; a name with no match at all is left
 * null (imported as uncategorized) rather than auto-creating category hierarchy. Label names are
 * passed through as-is — the import use case finds-or-creates a matching [com.expensetracker.wallet
 * .domain.model.Label] per row, same policy as accounts.
 */
class MapCsvRowsUseCase @Inject constructor() {
    operator fun invoke(
        bodyRows: List<List<String>>,
        mapping: Map<ImportColumn, Int?>,
        existingCategories: List<Category>,
        existingCategoryGroups: List<CategoryGroup>,
        existingTransactions: List<Transaction>,
        accountNameById: Map<String, String>,
        defaultCurrency: String,
    ): List<ParsedImportRow> {
        val categoryMatcher = CategoryMatcher(existingCategories, existingCategoryGroups)

        fun cell(row: List<String>, column: ImportColumn): String? {
            val index = mapping[column] ?: return null
            return row.getOrNull(index)?.trim()?.takeIf { it.isNotEmpty() }
        }

        return bodyRows.mapIndexed { index, row ->
            val errors = mutableListOf<String>()

            val dateRaw = cell(row, ImportColumn.DATE)
            val dateOnlyMillis = dateRaw?.let(::parseDate)
            if (dateRaw == null) errors.add("Missing date") else if (dateOnlyMillis == null) errors.add("Unrecognized date format")

            val timeRaw = cell(row, ImportColumn.TIME)
            val dateMillis = dateOnlyMillis?.let { combineDateAndTime(it, timeRaw?.let(::parseTimeOfDay)) }

            val amountRaw = cell(row, ImportColumn.AMOUNT)
            val signedAmountMinor = amountRaw?.let(::parseImportAmount)
            if (amountRaw == null) errors.add("Missing amount") else if (signedAmountMinor == null) errors.add("Invalid amount")

            val accountName = cell(row, ImportColumn.ACCOUNT)
            if (accountName == null) errors.add("Missing account")

            val categoryNameRaw = cell(row, ImportColumn.CATEGORY)

            val typeRaw = cell(row, ImportColumn.TYPE)
            val explicitType = typeRaw?.let(::parseType)
            if (typeRaw != null && explicitType == null) {
                errors.add("Unknown type (expected EXPENSE, INCOME, TRANSFER, REFUND, or ADJUSTMENT)")
            }
            val type = explicitType ?: when {
                categoryNameRaw?.contains("transfer", ignoreCase = true) == true -> TransactionType.TRANSFER
                signedAmountMinor != null && signedAmountMinor < 0 -> TransactionType.EXPENSE
                signedAmountMinor != null -> TransactionType.INCOME
                else -> null
            }

            // The app stores EXPENSE/INCOME as a positive magnitude (balance = opening + income -
            // expense) and only TRANSFER keeps the CSV's own signed value (outgoing negative,
            // incoming positive), matching CreateTransferUseCase's convention.
            val amountMinor = signedAmountMinor?.let { if (type == TransactionType.TRANSFER) it else kotlin.math.abs(it) }

            val categoryName = if (type == TransactionType.TRANSFER || categoryNameRaw == null) {
                null
            } else {
                categoryMatcher.match(categoryNameRaw, type ?: TransactionType.EXPENSE)?.name
            }

            val labelName = cell(row, ImportColumn.LABEL)
            val payee = cell(row, ImportColumn.PAYEE)
            val note = cell(row, ImportColumn.NOTE)
            val currency = cell(row, ImportColumn.CURRENCY)?.uppercase(Locale.US)
                ?: amountRaw?.let(::extractCurrencyCode)
                ?: defaultCurrency

            val isDuplicate = dateMillis != null && amountMinor != null && accountName != null &&
                existingTransactions.any { existing ->
                    // Minute-truncated: this app's own export writes time as "h:mm a" (no
                    // seconds), so a round-tripped date can differ from the original by up to
                    // 59s — an exact-millis match would miss every re-import of our own export.
                    existing.date / 60_000 == dateMillis / 60_000 &&
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
                labelName = labelName,
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
