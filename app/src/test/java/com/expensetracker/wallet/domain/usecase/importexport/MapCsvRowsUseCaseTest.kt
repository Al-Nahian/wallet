package com.expensetracker.wallet.domain.usecase.importexport

import com.expensetracker.wallet.domain.model.Category
import com.expensetracker.wallet.domain.model.CategoryGroup
import com.expensetracker.wallet.domain.model.CategoryType
import com.expensetracker.wallet.domain.model.Transaction
import com.expensetracker.wallet.domain.model.TransactionType
import java.text.SimpleDateFormat
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MapCsvRowsUseCaseTest {

    private val useCase = MapCsvRowsUseCase()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private fun date(s: String) = dateFormat.parse(s)!!.time

    private val expenseGroup = CategoryGroup(
        id = "g-expense",
        name = "Financial Expense",
        color = "#000000",
        icon = null,
        type = CategoryType.EXPENSE,
        sortOrder = 0,
        isSystem = true,
    )
    private val incomeGroup = CategoryGroup(
        id = "g-income",
        name = "Income",
        color = "#000000",
        icon = null,
        type = CategoryType.INCOME,
        sortOrder = 1,
        isSystem = true,
    )
    private val groceries = Category(id = "c-groceries", groupId = "g-expense", name = "Groceries", icon = null, sortOrder = 0, isSystem = true)
    private val financialExpense = Category(id = "c-finexp", groupId = "g-expense", name = "Financial Expense", icon = null, sortOrder = 1, isSystem = true)
    private val childSupportExpense = Category(id = "c-cs-expense", groupId = "g-expense", name = "Child Support", icon = null, sortOrder = 2, isSystem = true)
    private val childSupportIncome = Category(id = "c-cs-income", groupId = "g-income", name = "Child Support", icon = null, sortOrder = 0, isSystem = true)
    private val barCafe = Category(id = "c-barcafe", groupId = "g-expense", name = "Bar, Cafe", icon = null, sortOrder = 3, isSystem = true)

    private val categories = listOf(groceries, financialExpense, childSupportExpense, childSupportIncome, barCafe)
    private val groups = listOf(expenseGroup, incomeGroup)

    @Test
    fun `auto-detects a standard header row including label and time`() {
        val headers = listOf("Date", "Time", "Amount", "Type", "Account", "Category", "Label", "Payee", "Note", "Currency")
        val mapping = detectColumnMapping(headers)
        assertEquals(0, mapping[ImportColumn.DATE])
        assertEquals(1, mapping[ImportColumn.TIME])
        assertEquals(2, mapping[ImportColumn.AMOUNT])
        assertEquals(3, mapping[ImportColumn.TYPE])
        assertEquals(4, mapping[ImportColumn.ACCOUNT])
        assertEquals(5, mapping[ImportColumn.CATEGORY])
        assertEquals(6, mapping[ImportColumn.LABEL])
        assertEquals(7, mapping[ImportColumn.PAYEE])
        assertEquals(8, mapping[ImportColumn.NOTE])
        assertEquals(9, mapping[ImportColumn.CURRENCY])
    }

    private val mappingWithoutType = mapOf(
        ImportColumn.DATE to 0,
        ImportColumn.TIME to 1,
        ImportColumn.AMOUNT to 2,
        ImportColumn.ACCOUNT to 3,
        ImportColumn.CATEGORY to 4,
        ImportColumn.LABEL to 5,
        ImportColumn.NOTE to 6,
    )

    @Test
    fun `full-month date and 12-hour time combine into one timestamp`() {
        val rows = useCase(
            bodyRows = listOf(listOf("January 31, 2026", "12:45 PM", "-BDT 60.00", "Cash", "Bar, cafe", "Smoke", "")),
            mapping = mappingWithoutType,
            existingCategories = categories,
            existingCategoryGroups = groups,
            existingTransactions = emptyList(),
            accountNameById = emptyMap(),
            defaultCurrency = "BDT",
        )
        val row = rows.single()
        assertTrue(row.errors.isEmpty())
        val calendar = java.util.Calendar.getInstance().apply { timeInMillis = row.dateMillis!! }
        assertEquals(2026, calendar.get(java.util.Calendar.YEAR))
        assertEquals(java.util.Calendar.JANUARY, calendar.get(java.util.Calendar.MONTH))
        assertEquals(31, calendar.get(java.util.Calendar.DAY_OF_MONTH))
        assertEquals(12, calendar.get(java.util.Calendar.HOUR_OF_DAY))
        assertEquals(45, calendar.get(java.util.Calendar.MINUTE))
    }

    @Test
    fun `currency-prefixed negative amount parses as a positive expense magnitude`() {
        val rows = useCase(
            bodyRows = listOf(listOf("January 31, 2026", "12:45 PM", "-BDT 60.00", "Cash", "Bar, cafe", "Smoke", "")),
            mapping = mappingWithoutType,
            existingCategories = categories,
            existingCategoryGroups = groups,
            existingTransactions = emptyList(),
            accountNameById = emptyMap(),
            defaultCurrency = "BDT",
        )
        val row = rows.single()
        assertEquals(TransactionType.EXPENSE, row.type)
        assertEquals(6000L, row.amountMinor)
        assertEquals("BDT", row.currency)
    }

    @Test
    fun `currency-prefixed positive amount with thousands separator parses as income`() {
        val rows = useCase(
            bodyRows = listOf(listOf("January 22, 2026", "12:09 PM", "BDT 1,500.00", "Cash", "Lending, renting", "Lend", "")),
            mapping = mappingWithoutType,
            existingCategories = categories,
            existingCategoryGroups = groups,
            existingTransactions = emptyList(),
            accountNameById = emptyMap(),
            defaultCurrency = "BDT",
        )
        val row = rows.single()
        assertEquals(TransactionType.INCOME, row.type)
        assertEquals(150000L, row.amountMinor)
    }

    @Test
    fun `category containing 'transfer' becomes a TRANSFER with no category and keeps its sign`() {
        val rows = useCase(
            bodyRows = listOf(listOf("January 26, 2026", "6:56 PM", "BDT 350.00", "Bank", "Transfer, withdraw", "", "")),
            mapping = mappingWithoutType,
            existingCategories = categories,
            existingCategoryGroups = groups,
            existingTransactions = emptyList(),
            accountNameById = emptyMap(),
            defaultCurrency = "BDT",
        )
        val row = rows.single()
        assertEquals(TransactionType.TRANSFER, row.type)
        assertNull(row.categoryName)
        assertEquals(35000L, row.amountMinor)
    }

    @Test
    fun `category matching ignores punctuation and case`() {
        val rows = useCase(
            bodyRows = listOf(listOf("January 31, 2026", "12:45 PM", "-BDT 60.00", "Cash", "Bar, cafe", "Smoke", "")),
            mapping = mappingWithoutType,
            existingCategories = categories,
            existingCategoryGroups = groups,
            existingTransactions = emptyList(),
            accountNameById = emptyMap(),
            defaultCurrency = "BDT",
        )
        assertEquals("Bar, Cafe", rows.single().categoryName)
    }

    @Test
    fun `category matching tolerates a trailing plural`() {
        val rows = useCase(
            bodyRows = listOf(listOf("January 6, 2026", "2:37 PM", "-BDT 5030.00", "Bank", "Financial expenses", "", "")),
            mapping = mappingWithoutType,
            existingCategories = categories,
            existingCategoryGroups = groups,
            existingTransactions = emptyList(),
            accountNameById = emptyMap(),
            defaultCurrency = "BDT",
        )
        assertEquals("Financial Expense", rows.single().categoryName)
    }

    @Test
    fun `a category name shared by an expense and an income group picks the type-matching one`() {
        val expenseRow = useCase(
            bodyRows = listOf(listOf("January 18, 2026", "7:49 PM", "-BDT 10.00", "Cash", "Child Support", "", "")),
            mapping = mappingWithoutType,
            existingCategories = categories,
            existingCategoryGroups = groups,
            existingTransactions = emptyList(),
            accountNameById = emptyMap(),
            defaultCurrency = "BDT",
        ).single()
        assertEquals(childSupportExpense.name, expenseRow.categoryName)

        val incomeRow = useCase(
            bodyRows = listOf(listOf("January 18, 2026", "7:49 PM", "BDT 10.00", "Cash", "Child Support", "", "")),
            mapping = mappingWithoutType,
            existingCategories = categories,
            existingCategoryGroups = groups,
            existingTransactions = emptyList(),
            accountNameById = emptyMap(),
            defaultCurrency = "BDT",
        ).single()
        assertEquals(childSupportIncome.name, incomeRow.categoryName)
    }

    @Test
    fun `label cell passes through as-is`() {
        val rows = useCase(
            bodyRows = listOf(listOf("January 31, 2026", "12:45 PM", "-BDT 60.00", "Cash", "Bar, cafe", "Smoke", "")),
            mapping = mappingWithoutType,
            existingCategories = categories,
            existingCategoryGroups = groups,
            existingTransactions = emptyList(),
            accountNameById = emptyMap(),
            defaultCurrency = "BDT",
        )
        assertEquals("Smoke", rows.single().labelName)
    }

    @Test
    fun `an unmatched category is left uncategorized rather than erroring`() {
        val rows = useCase(
            bodyRows = listOf(listOf("January 1, 2026", "1:00 PM", "-BDT 10.00", "Cash", "Nonexistent Category", "", "")),
            mapping = mappingWithoutType,
            existingCategories = categories,
            existingCategoryGroups = groups,
            existingTransactions = emptyList(),
            accountNameById = emptyMap(),
            defaultCurrency = "BDT",
        )
        val row = rows.single()
        assertTrue(row.errors.isEmpty())
        assertNull(row.categoryName)
    }

    @Test
    fun `malformed date is flagged as a row error`() {
        val rows = useCase(
            bodyRows = listOf(listOf("not-a-date", "", "-BDT 60.00", "Cash", "", "", "")),
            mapping = mappingWithoutType,
            existingCategories = categories,
            existingCategoryGroups = groups,
            existingTransactions = emptyList(),
            accountNameById = emptyMap(),
            defaultCurrency = "BDT",
        )
        val row = rows.single()
        assertTrue(row.errors.any { it.contains("date", ignoreCase = true) })
        assertFalse(row.accepted)
    }

    @Test
    fun `malformed amount is flagged as a row error`() {
        val rows = useCase(
            bodyRows = listOf(listOf("January 1, 2026", "", "not-a-number", "Cash", "", "", "")),
            mapping = mappingWithoutType,
            existingCategories = categories,
            existingCategoryGroups = groups,
            existingTransactions = emptyList(),
            accountNameById = emptyMap(),
            defaultCurrency = "BDT",
        )
        assertTrue(rows.single().errors.any { it.contains("amount", ignoreCase = true) })
    }

    @Test
    fun `known-duplicate row is flagged and unaccepted by default`() {
        val existing = Transaction(
            id = "t1",
            accountId = "acc1",
            type = TransactionType.EXPENSE,
            amountMinor = 6000L,
            currency = "BDT",
            categoryId = null,
            payee = null,
            note = null,
            date = combine("2026-01-31", 12, 45),
            createdAt = 0L,
            updatedAt = 0L,
            isRecurring = false,
            deletedAt = null,
        )
        val rows = useCase(
            bodyRows = listOf(listOf("January 31, 2026", "12:45 PM", "-BDT 60.00", "Cash", "Bar, cafe", "Smoke", "")),
            mapping = mappingWithoutType,
            existingCategories = categories,
            existingCategoryGroups = groups,
            existingTransactions = listOf(existing),
            accountNameById = mapOf("acc1" to "Cash"),
            defaultCurrency = "BDT",
        )
        val row = rows.single()
        assertTrue(row.isDuplicate)
        assertFalse(row.accepted)
    }

    private fun combine(dateStr: String, hour: Int, minute: Int): Long =
        java.util.Calendar.getInstance().apply {
            timeInMillis = date(dateStr)
            set(java.util.Calendar.HOUR_OF_DAY, hour)
            set(java.util.Calendar.MINUTE, minute)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }.timeInMillis
}
