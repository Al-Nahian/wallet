package com.example.wallet.domain.usecase.importexport

import com.example.wallet.domain.model.Transaction
import com.example.wallet.domain.model.TransactionType
import java.text.SimpleDateFormat
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MapCsvRowsUseCaseTest {

    private val useCase = MapCsvRowsUseCase()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private fun date(s: String) = dateFormat.parse(s)!!.time

    @Test
    fun `auto-detects a standard header row`() {
        val headers = listOf("Date", "Amount", "Type", "Account", "Category", "Payee", "Note", "Currency")
        val mapping = detectColumnMapping(headers)
        assertEquals(0, mapping[ImportColumn.DATE])
        assertEquals(1, mapping[ImportColumn.AMOUNT])
        assertEquals(2, mapping[ImportColumn.TYPE])
        assertEquals(3, mapping[ImportColumn.ACCOUNT])
        assertEquals(4, mapping[ImportColumn.CATEGORY])
        assertEquals(5, mapping[ImportColumn.PAYEE])
        assertEquals(6, mapping[ImportColumn.NOTE])
        assertEquals(7, mapping[ImportColumn.CURRENCY])
    }

    @Test
    fun `auto-detection leaves unmatched columns null`() {
        val mapping = detectColumnMapping(listOf("Some Weird Header"))
        assertTrue(mapping.values.all { it == null })
    }

    private val standardMapping = mapOf(
        ImportColumn.DATE to 0,
        ImportColumn.AMOUNT to 1,
        ImportColumn.TYPE to 2,
        ImportColumn.ACCOUNT to 3,
        ImportColumn.CATEGORY to 4,
        ImportColumn.PAYEE to 5,
        ImportColumn.NOTE to 6,
        ImportColumn.CURRENCY to 7,
    )

    @Test
    fun `well-formed row parses with no errors and is accepted`() {
        val rows = useCase(
            bodyRows = listOf(listOf("2026-01-01", "100.00", "EXPENSE", "Checking", "Groceries", "Store", "", "BDT")),
            mapping = standardMapping,
            existingCategoryNames = setOf("Groceries"),
            existingTransactions = emptyList(),
            accountNameById = emptyMap(),
            defaultCurrency = "BDT",
        )
        val row = rows.single()
        assertTrue(row.errors.isEmpty())
        assertTrue(row.accepted)
        assertEquals(date("2026-01-01"), row.dateMillis)
        assertEquals(10000L, row.amountMinor)
        assertEquals(TransactionType.EXPENSE, row.type)
        assertEquals("Groceries", row.categoryName)
    }

    @Test
    fun `malformed date is flagged as a row error`() {
        val rows = useCase(
            bodyRows = listOf(listOf("not-a-date", "100.00", "EXPENSE", "Checking", "", "", "", "")),
            mapping = standardMapping,
            existingCategoryNames = emptySet(),
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
            bodyRows = listOf(listOf("2026-01-01", "not-a-number", "EXPENSE", "Checking", "", "", "", "")),
            mapping = standardMapping,
            existingCategoryNames = emptySet(),
            existingTransactions = emptyList(),
            accountNameById = emptyMap(),
            defaultCurrency = "BDT",
        )
        assertTrue(rows.single().errors.any { it.contains("amount", ignoreCase = true) })
    }

    @Test
    fun `unknown category is imported as uncategorized, not an error`() {
        val rows = useCase(
            bodyRows = listOf(listOf("2026-01-01", "100.00", "EXPENSE", "Checking", "Nonexistent", "", "", "")),
            mapping = standardMapping,
            existingCategoryNames = setOf("Groceries"),
            existingTransactions = emptyList(),
            accountNameById = emptyMap(),
            defaultCurrency = "BDT",
        )
        val row = rows.single()
        assertTrue(row.errors.isEmpty())
        assertEquals(null, row.categoryName)
    }

    @Test
    fun `known-duplicate row is flagged and unaccepted by default`() {
        val existing = Transaction(
            id = "t1",
            accountId = "acc1",
            type = TransactionType.EXPENSE,
            amountMinor = 10000L,
            currency = "BDT",
            categoryId = null,
            payee = "Store",
            note = null,
            date = date("2026-01-01"),
            createdAt = 0L,
            updatedAt = 0L,
            isRecurring = false,
            deletedAt = null,
        )
        val rows = useCase(
            bodyRows = listOf(listOf("2026-01-01", "100.00", "EXPENSE", "Checking", "", "Store", "", "")),
            mapping = standardMapping,
            existingCategoryNames = emptySet(),
            existingTransactions = listOf(existing),
            accountNameById = mapOf("acc1" to "Checking"),
            defaultCurrency = "BDT",
        )
        val row = rows.single()
        assertTrue(row.isDuplicate)
        assertFalse(row.accepted)
    }

    @Test
    fun `known-new row is not flagged as duplicate`() {
        val existing = Transaction(
            id = "t1",
            accountId = "acc1",
            type = TransactionType.EXPENSE,
            amountMinor = 10000L,
            currency = "BDT",
            categoryId = null,
            payee = "Store",
            note = null,
            date = date("2026-01-01"),
            createdAt = 0L,
            updatedAt = 0L,
            isRecurring = false,
            deletedAt = null,
        )
        val rows = useCase(
            bodyRows = listOf(listOf("2026-02-01", "100.00", "EXPENSE", "Checking", "", "Store", "", "")),
            mapping = standardMapping,
            existingCategoryNames = emptySet(),
            existingTransactions = listOf(existing),
            accountNameById = mapOf("acc1" to "Checking"),
            defaultCurrency = "BDT",
        )
        assertFalse(rows.single().isDuplicate)
    }
}
