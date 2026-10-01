package com.expensetracker.wallet.domain.usecase.importexport

import com.expensetracker.wallet.core.common.parseMoneyToMinorUnits
import com.expensetracker.wallet.domain.model.Account
import com.expensetracker.wallet.domain.model.AccountType
import com.expensetracker.wallet.domain.model.Budget
import com.expensetracker.wallet.domain.model.BudgetPeriod
import com.expensetracker.wallet.domain.model.Category
import com.expensetracker.wallet.domain.model.CategoryGroup
import com.expensetracker.wallet.domain.model.CategoryType
import com.expensetracker.wallet.domain.model.Label
import com.expensetracker.wallet.domain.model.Template
import java.text.SimpleDateFormat
import java.util.Locale

/** This app's own export always writes dates as "January 31, 2026"; a couple of other common
 * spellings are accepted too, matching [MapCsvRowsUseCase]'s date parsing. */
private val DATE_FORMATS = listOf("MMMM d, yyyy", "yyyy-MM-dd", "yyyy/MM/dd", "MM/dd/yyyy", "dd/MM/yyyy")

private fun parseExportDate(raw: String): Long? {
    val trimmed = raw.trim()
    if (trimmed.isEmpty()) return null
    for (pattern in DATE_FORMATS) {
        val format = SimpleDateFormat(pattern, Locale.US).apply { isLenient = false }
        runCatching { format.parse(trimmed) }.getOrNull()?.let { return it.time }
    }
    return null
}

private fun cell(row: List<String>, index: Int): String? = row.getOrNull(index)?.trim()?.takeIf { it.isNotEmpty() }

data class ParsedAccountRow(
    val rowIndex: Int,
    val name: String?,
    val type: AccountType,
    val institutionName: String?,
    val currency: String,
    val openingBalanceMinor: Long,
    val isArchived: Boolean,
    val errors: List<String>,
    val isDuplicate: Boolean,
    val accepted: Boolean,
)

/** Header: name,type,institution,currency,openingBalance,archived ([ExportCsv.accountsToCsv]). */
fun parseAccountRows(bodyRows: List<List<String>>, existingAccounts: List<Account>): List<ParsedAccountRow> {
    val existingNames = existingAccounts.map { it.name.trim().lowercase(Locale.US) }.toSet()
    return bodyRows.mapIndexed { index, row ->
        val errors = mutableListOf<String>()
        val name = cell(row, 0)
        if (name == null) errors.add("Missing name")

        val typeRaw = cell(row, 1)
        val type = typeRaw?.let { runCatching { AccountType.valueOf(it.uppercase(Locale.US)) }.getOrNull() }
        if (typeRaw != null && type == null) errors.add("Unknown account type")

        val institutionName = cell(row, 2)
        val currency = cell(row, 3) ?: "BDT"
        val openingBalanceRaw = cell(row, 4)
        val openingBalanceMinor = openingBalanceRaw?.let(::parseMoneyToMinorUnits)
        if (openingBalanceRaw != null && openingBalanceMinor == null) errors.add("Invalid opening balance")
        val isArchived = cell(row, 5)?.toBooleanStrictOrNull() ?: false

        val isDuplicate = name != null && name.lowercase(Locale.US) in existingNames

        ParsedAccountRow(
            rowIndex = index,
            name = name,
            type = type ?: AccountType.OTHER,
            institutionName = institutionName,
            currency = currency,
            openingBalanceMinor = openingBalanceMinor ?: 0L,
            isArchived = isArchived,
            errors = errors,
            isDuplicate = isDuplicate,
            accepted = errors.isEmpty() && !isDuplicate,
        )
    }
}

data class ParsedCategoryRow(
    val rowIndex: Int,
    val groupName: String?,
    val groupType: CategoryType?,
    val categoryName: String?,
    val errors: List<String>,
    val isDuplicate: Boolean,
    val accepted: Boolean,
)

/** Header: group,groupType,category ([ExportCsv.categoriesToCsv]). Category groups are a fixed,
 * seeded taxonomy in this app (no "create group" feature anywhere), so a row's group must match
 * an existing one by name+type — it's a validation error otherwise, not an auto-created group. */
fun parseCategoryRows(
    bodyRows: List<List<String>>,
    existingGroups: List<CategoryGroup>,
    existingCategories: List<Category>,
): List<ParsedCategoryRow> {
    val groupByKey = existingGroups.associateBy { it.name.trim().lowercase(Locale.US) to it.type }
    val categoryNamesByGroup = existingCategories.groupBy { it.groupId }
        .mapValues { (_, cats) -> cats.map { it.name.trim().lowercase(Locale.US) }.toSet() }

    return bodyRows.mapIndexed { index, row ->
        val errors = mutableListOf<String>()
        val groupName = cell(row, 0)
        if (groupName == null) errors.add("Missing group")

        val groupTypeRaw = cell(row, 1)
        val groupType = groupTypeRaw?.let { runCatching { CategoryType.valueOf(it.uppercase(Locale.US)) }.getOrNull() }
        if (groupTypeRaw != null && groupType == null) errors.add("Unknown group type (expected EXPENSE or INCOME)")

        val categoryName = cell(row, 2)
        if (categoryName == null) errors.add("Missing category")

        val matchedGroup = if (groupName != null && groupType != null) {
            groupByKey[groupName.lowercase(Locale.US) to groupType]
        } else {
            null
        }
        if (errors.isEmpty() && matchedGroup == null) {
            errors.add("No existing \"$groupName\" ${groupType?.name?.lowercase(Locale.US)} group — categories can only be added under existing groups")
        }

        val isDuplicate = matchedGroup != null && categoryName != null &&
            categoryName.lowercase(Locale.US) in categoryNamesByGroup[matchedGroup.id].orEmpty()

        ParsedCategoryRow(
            rowIndex = index,
            groupName = matchedGroup?.name ?: groupName,
            groupType = groupType,
            categoryName = categoryName,
            errors = errors,
            isDuplicate = isDuplicate,
            accepted = errors.isEmpty() && !isDuplicate,
        )
    }
}

data class ParsedLabelRow(
    val rowIndex: Int,
    val name: String?,
    val color: String,
    val errors: List<String>,
    val isDuplicate: Boolean,
    val accepted: Boolean,
)

/** Header: name,color ([ExportCsv.labelsToCsv]). */
fun parseLabelRows(bodyRows: List<List<String>>, existingLabels: List<Label>): List<ParsedLabelRow> {
    val existingNames = existingLabels.map { it.name.trim().lowercase(Locale.US) }.toSet()
    return bodyRows.mapIndexed { index, row ->
        val errors = mutableListOf<String>()
        val name = cell(row, 0)
        if (name == null) errors.add("Missing name")
        val color = cell(row, 1) ?: "#9E9E9E"
        val isDuplicate = name != null && name.lowercase(Locale.US) in existingNames

        ParsedLabelRow(
            rowIndex = index,
            name = name,
            color = color,
            errors = errors,
            isDuplicate = isDuplicate,
            accepted = errors.isEmpty() && !isDuplicate,
        )
    }
}

data class ParsedBudgetRow(
    val rowIndex: Int,
    val name: String?,
    val period: BudgetPeriod?,
    val startDate: Long?,
    val endDate: Long?,
    val amountMinor: Long?,
    val currency: String,
    val errors: List<String>,
    val isDuplicate: Boolean,
    val accepted: Boolean,
)

/** Header: name,period,startDate,endDate,amount,currency ([ExportCsv.budgetsToCsv]). Category
 * limits aren't part of the export (plans/12-import-export.md's export doesn't cover
 * [com.expensetracker.wallet.domain.model.BudgetCategory]), so an imported budget always starts
 * with none — the same gap the export side already has. */
fun parseBudgetRows(
    bodyRows: List<List<String>>,
    existingBudgets: List<Budget>,
): List<ParsedBudgetRow> {
    return bodyRows.mapIndexed { index, row ->
        val errors = mutableListOf<String>()
        val name = cell(row, 0)
        if (name == null) errors.add("Missing name")

        val periodRaw = cell(row, 1)
        val period = periodRaw?.let { runCatching { BudgetPeriod.valueOf(it.uppercase(Locale.US)) }.getOrNull() }
        if (periodRaw != null && period == null) errors.add("Unknown period")

        val startDateRaw = cell(row, 2)
        val startDate = startDateRaw?.let(::parseExportDate)
        if (startDateRaw == null) errors.add("Missing start date") else if (startDate == null) errors.add("Unrecognized start date")

        val endDateRaw = cell(row, 3)
        val endDate = endDateRaw?.let(::parseExportDate)
        if (endDateRaw == null) errors.add("Missing end date") else if (endDate == null) errors.add("Unrecognized end date")

        val amountRaw = cell(row, 4)
        val amountMinor = amountRaw?.let(::parseMoneyToMinorUnits)
        if (amountRaw == null) errors.add("Missing amount") else if (amountMinor == null) errors.add("Invalid amount")

        val currency = cell(row, 5) ?: "BDT"

        val isDuplicate = name != null && startDate != null &&
            existingBudgets.any {
                it.name.trim().lowercase(Locale.US) == name.lowercase(Locale.US) && it.startDate == startDate
            }

        ParsedBudgetRow(
            rowIndex = index,
            name = name,
            period = period,
            startDate = startDate,
            endDate = endDate,
            amountMinor = amountMinor,
            currency = currency,
            errors = errors,
            isDuplicate = isDuplicate,
            accepted = errors.isEmpty() && !isDuplicate,
        )
    }
}

data class ParsedTemplateRow(
    val rowIndex: Int,
    val name: String?,
    val accountName: String?,
    val categoryName: String?,
    val labelName: String?,
    val payee: String?,
    val place: String?,
    val errors: List<String>,
    val isDuplicate: Boolean,
    val accepted: Boolean,
)

/** Header: name,account,category,label,payee,place ([ExportCsv.templatesToCsv]). A template
 * requires an account, category, and label (same as [com.expensetracker.wallet.domain.usecase
 * .template.CreateTemplateUseCase]), matched by name against what already exists — unlike an
 * import row's account name, these are never auto-created, since a template pointing at a
 * made-up account/category/label would be useless. */
fun parseTemplateRows(
    bodyRows: List<List<String>>,
    existingAccounts: List<Account>,
    existingCategories: List<Category>,
    existingLabels: List<Label>,
    existingTemplates: List<Template>,
): List<ParsedTemplateRow> {
    val accountIdByLowerName = existingAccounts.associateBy { it.name.trim().lowercase(Locale.US) }
    val categoryIdByLowerName = existingCategories.associateBy { it.name.trim().lowercase(Locale.US) }
    val labelIdByLowerName = existingLabels.associateBy { it.name.trim().lowercase(Locale.US) }
    val existingNames = existingTemplates.map { it.name.trim().lowercase(Locale.US) }.toSet()

    return bodyRows.mapIndexed { index, row ->
        val errors = mutableListOf<String>()
        val name = cell(row, 0)
        if (name == null) errors.add("Missing name")

        val accountName = cell(row, 1)
        if (accountName == null) {
            errors.add("Missing account")
        } else if (accountName.lowercase(Locale.US) !in accountIdByLowerName) {
            errors.add("No existing account named \"$accountName\"")
        }

        val categoryName = cell(row, 2)
        if (categoryName == null) {
            errors.add("Missing category")
        } else if (categoryName.lowercase(Locale.US) !in categoryIdByLowerName) {
            errors.add("No existing category named \"$categoryName\"")
        }

        val labelName = cell(row, 3)
        if (labelName == null) {
            errors.add("Missing label")
        } else if (labelName.lowercase(Locale.US) !in labelIdByLowerName) {
            errors.add("No existing label named \"$labelName\"")
        }

        val payee = cell(row, 4)
        val place = cell(row, 5)
        val isDuplicate = name != null && name.lowercase(Locale.US) in existingNames

        ParsedTemplateRow(
            rowIndex = index,
            name = name,
            accountName = accountName,
            categoryName = categoryName,
            labelName = labelName,
            payee = payee,
            place = place,
            errors = errors,
            isDuplicate = isDuplicate,
            accepted = errors.isEmpty() && !isDuplicate,
        )
    }
}
