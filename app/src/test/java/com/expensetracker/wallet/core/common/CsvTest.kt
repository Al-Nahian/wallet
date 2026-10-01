package com.expensetracker.wallet.core.common

import org.junit.Assert.assertEquals
import org.junit.Test

class CsvTest {

    @Test
    fun `parses a simple well-formed file`() {
        val text = "date,amount,type\n2026-01-01,100.00,EXPENSE\n2026-01-02,50.00,INCOME\n"
        val rows = Csv.parse(text)
        assertEquals(3, rows.size)
        assertEquals(listOf("date", "amount", "type"), rows[0])
        assertEquals(listOf("2026-01-01", "100.00", "EXPENSE"), rows[1])
        assertEquals(listOf("2026-01-02", "50.00", "INCOME"), rows[2])
    }

    @Test
    fun `handles quoted fields containing commas`() {
        val text = "note\n\"Lunch, coffee\"\n"
        val rows = Csv.parse(text)
        assertEquals(listOf("note"), rows[0])
        assertEquals(listOf("Lunch, coffee"), rows[1])
    }

    @Test
    fun `handles escaped quotes inside a quoted field`() {
        val text = "note\n\"She said \"\"hi\"\"\"\n"
        val rows = Csv.parse(text)
        assertEquals(listOf("She said \"hi\""), rows[1])
    }

    @Test
    fun `handles quoted fields containing embedded newlines`() {
        val text = "note\n\"line one\nline two\"\n"
        val rows = Csv.parse(text)
        assertEquals(2, rows.size)
        assertEquals(listOf("line one\nline two"), rows[1])
    }

    @Test
    fun `tolerates extra columns per row`() {
        val text = "a,b\n1,2,3\n"
        val rows = Csv.parse(text)
        assertEquals(listOf("1", "2", "3"), rows[1])
    }

    @Test
    fun `tolerates missing trailing columns per row`() {
        val text = "a,b,c\n1,2\n"
        val rows = Csv.parse(text)
        assertEquals(listOf("1", "2"), rows[1])
    }

    @Test
    fun `handles a file with no trailing newline`() {
        val text = "a,b\n1,2"
        val rows = Csv.parse(text)
        assertEquals(2, rows.size)
        assertEquals(listOf("1", "2"), rows[1])
    }

    @Test
    fun `write then parse round-trips values with special characters`() {
        val original = listOf(
            listOf("date", "note"),
            listOf("2026-01-01", "has, a comma"),
            listOf("2026-01-02", "has a \"quote\""),
            listOf("2026-01-03", "has\na newline"),
        )
        val csv = Csv.write(original)
        val parsed = Csv.parse(csv)
        assertEquals(original, parsed)
    }
}
