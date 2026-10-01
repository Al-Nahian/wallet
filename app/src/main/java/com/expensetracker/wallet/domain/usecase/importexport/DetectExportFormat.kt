package com.expensetracker.wallet.domain.usecase.importexport

import java.util.Locale

/** The exact header row [ExportCsv] writes for each entity, in order — used to recognize a file
 * as this app's own export so it can be routed straight to the matching import path instead of
 * the generic transaction column-mapping wizard. */
private val FORMAT_HEADERS: Map<ExportEntityType, List<String>> = mapOf(
    ExportEntityType.TRANSACTIONS to
        listOf("date", "amount", "type", "account", "category", "label", "payee", "note", "currency", "time"),
    ExportEntityType.ACCOUNTS to listOf("name", "type", "institution", "currency", "openingbalance", "archived"),
    ExportEntityType.BUDGETS to listOf("name", "period", "startdate", "enddate", "amount", "currency"),
    ExportEntityType.CATEGORIES to listOf("group", "grouptype", "category"),
    ExportEntityType.LABELS to listOf("name", "color"),
    ExportEntityType.TEMPLATES to listOf("name", "account", "category", "label", "payee", "place"),
)

/** Null means "not recognized as one of this app's own exports" — the caller falls back to the
 * generic column-mapping flow (works for this app's own transaction export too, just via mapping
 * instead of a direct match, and for arbitrary third-party transaction CSVs). */
fun detectExportFormat(headers: List<String>): ExportEntityType? {
    val normalized = headers.map { it.trim().lowercase(Locale.US) }
    return FORMAT_HEADERS.entries.firstOrNull { (_, expected) -> normalized == expected }?.key
}
