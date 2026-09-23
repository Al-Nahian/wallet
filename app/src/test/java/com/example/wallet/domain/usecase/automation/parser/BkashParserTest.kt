package com.example.wallet.domain.usecase.automation.parser

import com.example.wallet.domain.model.ParseConfidence
import com.example.wallet.domain.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BkashParserTest {

    private val parser = BkashParser()
    private val now = 1_700_000_000_000L

    @Test
    fun `does not handle a message from a non-bKash sender`() {
        assertTrue(!parser.canHandle("SomeBank", "Payment Tk 500.00 to Store successful. TrxID ABC123"))
    }

    @Test
    fun `parses a merchant payment as an expense`() {
        val body = "Payment Tk 500.00 to Star Tea Stall successful. Fee Tk 0.00. Balance Tk 4,500.00. TrxID 8N7A1B2C3D at 22/09/2026 10:15"
        val result = parser.parse("bKash", body, now)!!

        assertEquals(TransactionType.EXPENSE, result.type)
        assertEquals(50000L, result.amountMinor)
        assertEquals(AccountHint.BKASH, result.accountHint)
        assertNull(result.counterAccountHint)
        assertEquals("Star Tea Stall", result.merchant)
        assertEquals("8N7A1B2C3D", result.referenceId)
        assertEquals(ParseConfidence.HIGH, result.baseConfidence)
    }

    @Test
    fun `parses a cash out as a transfer from bKash to cash`() {
        val body = "Cash Out Tk 1,000.00 from agent 01712345678 successful. Fee Tk 18.50. Balance Tk 3,481.50. TrxID XYZ999 at 22/09/2026 10:15"
        val result = parser.parse("bKash", body, now)!!

        assertEquals(TransactionType.TRANSFER, result.type)
        assertEquals(100000L, result.amountMinor)
        assertEquals(AccountHint.BKASH, result.accountHint)
        assertEquals(AccountHint.CASH, result.counterAccountHint)
        assertEquals("XYZ999", result.referenceId)
    }

    @Test
    fun `parses add money as a transfer from bank to bKash`() {
        val body = "Tk 2,000.00 added to your bKash account from your Bank account. Balance Tk 6,000.00. TrxID ADD111 at 22/09/2026 10:15"
        val result = parser.parse("bKash", body, now)!!

        assertEquals(TransactionType.TRANSFER, result.type)
        assertEquals(AccountHint.BANK, result.accountHint)
        assertEquals(AccountHint.BKASH, result.counterAccountHint)
        assertEquals(200000L, result.amountMinor)
    }

    @Test
    fun `parses received money as income with the sender phone as merchant`() {
        val body = "You have received Tk 1,000.00 from 01812345678. Fee Tk 0.00. Balance Tk 5,500.00. TrxID RCV222 at 22/09/2026 10:15"
        val result = parser.parse("bKash", body, now)!!

        assertEquals(TransactionType.INCOME, result.type)
        assertEquals(AccountHint.BKASH, result.accountHint)
        assertEquals("01812345678", result.merchant)
        assertEquals(100000L, result.amountMinor)
    }

    @Test
    fun `parses sent money as an expense`() {
        val body = "You have sent Tk 500.00 to 01912345678. Fee Tk 5.00. Balance Tk 4,995.00. TrxID SND333 at 22/09/2026 10:15"
        val result = parser.parse("bKash", body, now)!!

        assertEquals(TransactionType.EXPENSE, result.type)
        assertEquals(AccountHint.BKASH, result.accountHint)
        assertEquals("01912345678", result.merchant)
    }

    @Test
    fun `an unrecognized bKash message returns null instead of guessing`() {
        val body = "Your bKash account has been temporarily suspended. Please visit your nearest agent."
        assertNull(parser.parse("bKash", body, now))
    }
}
