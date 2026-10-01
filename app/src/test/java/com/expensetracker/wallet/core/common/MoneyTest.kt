package com.expensetracker.wallet.core.common

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MoneyTest {

    @Test
    fun `parses whole numbers`() {
        assertEquals(100_000_00L, parseMoneyToMinorUnits("100000"))
    }

    @Test
    fun `parses decimals`() {
        assertEquals(100_000_50L, parseMoneyToMinorUnits("100000.50"))
        assertEquals(100_000_05L, parseMoneyToMinorUnits("100000.05"))
    }

    @Test
    fun `parses a single decimal digit as tenths`() {
        assertEquals(100_000_50L, parseMoneyToMinorUnits("100000.5"))
    }

    @Test
    fun `strips thousands separators`() {
        assertEquals(100_000_00L, parseMoneyToMinorUnits("100,000"))
    }

    @Test
    fun `parses negative amounts`() {
        assertEquals(-50000L, parseMoneyToMinorUnits("-500.00"))
    }

    @Test
    fun `rejects non-numeric input`() {
        assertNull(parseMoneyToMinorUnits("not-a-number"))
        assertNull(parseMoneyToMinorUnits(""))
        assertNull(parseMoneyToMinorUnits("12.345"))
    }

    @Test
    fun `formats minor units with grouping and two decimals`() {
        assertEquals("BDT 100,000.50", formatMoney(100_000_50L, "BDT"))
        assertEquals("BDT 0.00", formatMoney(0L, "BDT"))
        assertEquals("BDT -500.00", formatMoney(-50000L, "BDT"))
        assertEquals("BDT 1,234,567.89", formatMoney(123_456_789L, "BDT"))
    }
}
