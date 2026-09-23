package com.example.wallet.domain.usecase.importexport

import com.example.wallet.core.common.Csv
import com.example.wallet.core.common.minorUnitsToEditableString
import com.example.wallet.domain.model.Account
import com.example.wallet.domain.model.Budget
import com.example.wallet.domain.model.Category
import com.example.wallet.domain.model.CategoryGroup
import com.example.wallet.domain.model.Institution
import com.example.wallet.domain.model.Label
import com.example.wallet.domain.model.Transaction
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Pure CSV formatting for the five exportable entities (plans/12-import-export.md §37) — no
 * repository access here, callers fetch the domain lists and pass them in. The transactions
 * columns match [ImportColumn] exactly (`date,amount,type,account,category,payee,note,currency`)
 * so an exported file can be re-imported unmodified.
 */
object ExportCsv {
    private val DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val TIME_FORMAT = SimpleDateFormat("h:mm a", Locale.US)

    private fun formatDate(millis: Long): String = DATE_FORMAT.format(millis)
    private fun formatTime(millis: Long): String = TIME_FORMAT.format(millis)

    fun transactionsToCsv(
        transactions: List<Transaction>,
        accountNameById: Map<String, String>,
        categoryNameById: Map<String, String>,
        labelNamesByTransactionId: Map<String, List<Label>> = emptyMap(),
    ): String {
        val header = listOf(
            "date", "amount", "type", "account", "category", "label", "payee", "note", "currency", "time",
        )
        val rows = transactions.map { tx ->
            listOf(
                formatDate(tx.date),
                minorUnitsToEditableString(tx.amountMinor),
                tx.type.name,
                accountNameById[tx.accountId].orEmpty(),
                tx.categoryId?.let { categoryNameById[it] }.orEmpty(),
                labelNamesByTransactionId[tx.id].orEmpty().joinToString(", ") { it.name },
                tx.payee.orEmpty(),
                tx.note.orEmpty(),
                tx.currency,
                formatTime(tx.date),
            )
        }
        return Csv.write(listOf(header) + rows)
    }

    fun accountsToCsv(accounts: List<Account>, institutionById: Map<String, Institution>): String {
        val header = listOf("name", "type", "institution", "currency", "openingBalance", "archived")
        val rows = accounts.map { account ->
            listOf(
                account.name,
                account.type.name,
                account.institutionId?.let { institutionById[it]?.name }.orEmpty(),
                account.currency,
                minorUnitsToEditableString(account.openingBalanceMinor),
                account.isArchived.toString(),
            )
        }
        return Csv.write(listOf(header) + rows)
    }

    fun budgetsToCsv(budgets: List<Budget>): String {
        val header = listOf("name", "period", "startDate", "endDate", "amount", "currency")
        val rows = budgets.map { budget ->
            listOf(
                budget.name,
                budget.period.name,
                formatDate(budget.startDate),
                formatDate(budget.endDate),
                minorUnitsToEditableString(budget.amountMinor),
                budget.currency,
            )
        }
        return Csv.write(listOf(header) + rows)
    }

    fun categoriesToCsv(groups: List<CategoryGroup>, categories: List<Category>): String {
        val header = listOf("group", "groupType", "category")
        val groupById = groups.associateBy { it.id }
        val rows = categories.map { category ->
            val group = groupById[category.groupId]
            listOf(group?.name.orEmpty(), group?.type?.name.orEmpty(), category.name)
        }
        return Csv.write(listOf(header) + rows)
    }

    fun labelsToCsv(labels: List<Label>): String {
        val header = listOf("name", "color")
        val rows = labels.map { label -> listOf(label.name, label.color) }
        return Csv.write(listOf(header) + rows)
    }
}
