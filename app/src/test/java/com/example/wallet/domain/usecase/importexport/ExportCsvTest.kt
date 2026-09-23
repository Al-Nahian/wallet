package com.example.wallet.domain.usecase.importexport

import com.example.wallet.core.common.Csv
import com.example.wallet.domain.model.Label
import com.example.wallet.domain.model.Transaction
import com.example.wallet.domain.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Test

class ExportCsvTest {

    @Test
    fun `transactions export produces a header matching the import column order`() {
        val csv = ExportCsv.transactionsToCsv(emptyList(), emptyMap(), emptyMap())
        val header = Csv.parse(csv).first()
        assertEquals(listOf("date", "amount", "type", "account", "category", "payee", "note", "currency"), header)
    }

    @Test
    fun `a known transaction round-trips through export then re-parse`() {
        val tx = Transaction(
            id = "t1",
            accountId = "acc1",
            type = TransactionType.EXPENSE,
            amountMinor = 12345L,
            currency = "BDT",
            categoryId = "cat1",
            payee = "Corner Store",
            note = "Snacks, drinks",
            date = 1_735_689_600_000L,
            createdAt = 0L,
            updatedAt = 0L,
            isRecurring = false,
            deletedAt = null,
        )
        val csv = ExportCsv.transactionsToCsv(
            listOf(tx),
            mapOf("acc1" to "Checking"),
            mapOf("cat1" to "Snacks"),
        )
        val rows = Csv.parse(csv)
        val dataRow = rows[1]
        assertEquals("123.45", dataRow[1])
        assertEquals("EXPENSE", dataRow[2])
        assertEquals("Checking", dataRow[3])
        assertEquals("Snacks", dataRow[4])
        assertEquals("Corner Store", dataRow[5])
        assertEquals("Snacks, drinks", dataRow[6])
        assertEquals("BDT", dataRow[7])
    }

    @Test
    fun `labels export writes name and color`() {
        val csv = ExportCsv.labelsToCsv(listOf(Label(id = "l1", name = "Work", color = "#FF0000", createdAt = 0L)))
        val rows = Csv.parse(csv)
        assertEquals(listOf("name", "color"), rows[0])
        assertEquals(listOf("Work", "#FF0000"), rows[1])
    }
}
