package com.example.wallet.core.design.components

import org.junit.Assert.assertEquals
import org.junit.Test

class AmountEntryFormatTest {

    @Test
    fun `empty input shows bare zero`() {
        assertEquals("0", formatAmountEntryForDisplay(""))
    }

    @Test
    fun `whole numbers show with no decimals`() {
        assertEquals("5", formatAmountEntryForDisplay("5"))
        assertEquals("50", formatAmountEntryForDisplay("50"))
        assertEquals("500", formatAmountEntryForDisplay("500"))
    }

    @Test
    fun `thousands group internationally`() {
        assertEquals("1,000", formatAmountEntryForDisplay("1000"))
        assertEquals("12,500", formatAmountEntryForDisplay("12500"))
        assertEquals("1,250,000", formatAmountEntryForDisplay("1250000"))
    }

    @Test
    fun `typed decimals show, with the pending dot preserved`() {
        assertEquals("1.45", formatAmountEntryForDisplay("1.45"))
        assertEquals("50.", formatAmountEntryForDisplay("50."))
        assertEquals("50.5", formatAmountEntryForDisplay("50.5"))
        assertEquals("1,000.5", formatAmountEntryForDisplay("1000.5"))
        assertEquals("50.10", formatAmountEntryForDisplay("50.10"))
    }

    @Test
    fun `all-zero fractions are dropped`() {
        assertEquals("50", formatAmountEntryForDisplay("50.00"))
        assertEquals("0", formatAmountEntryForDisplay("0.00"))
        assertEquals("1,000", formatAmountEntryForDisplay("1000.00"))
    }

    @Test
    fun `leading dot and leading zeros normalize naturally`() {
        assertEquals("0.5", formatAmountEntryForDisplay(".5"))
        assertEquals("0.", formatAmountEntryForDisplay("."))
        assertEquals("7", formatAmountEntryForDisplay("007"))
        assertEquals("0", formatAmountEntryForDisplay("0"))
    }

    @Test
    fun `each side of an operator formats independently`() {
        assertEquals("120+35", formatAmountEntryForDisplay("120+35"))
        assertEquals("120+", formatAmountEntryForDisplay("120+"))
        assertEquals("1,000×2", formatAmountEntryForDisplay("1000×2"))
        assertEquals("1.5+2.45", formatAmountEntryForDisplay("1.5+2.45"))
    }
}
