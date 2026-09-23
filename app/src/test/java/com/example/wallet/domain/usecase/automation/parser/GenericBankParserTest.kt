package com.example.wallet.domain.usecase.automation.parser

import com.example.wallet.domain.model.ParseConfidence
import com.example.wallet.domain.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GenericBankParserTest {

    private val parser = GenericBankParser()
    private val now = 1_700_000_000_000L

    @Test
    fun `does not handle a message with no banking keywords`() {
        assertTrue(!parser.canHandle("VendorX", "Your OTP is 123456. Do not share it with anyone."))
    }

    @Test
    fun `an ATM withdrawal becomes a transfer from bank to cash, always low confidence`() {
        val body = "Tk 5,000.00 withdrawn from your account XXXX1234 at ATM on 22-09-2026. Available balance Tk 45,000.00"
        val result = parser.parse("MyBank", body, now)!!

        assertEquals(TransactionType.TRANSFER, result.type)
        assertEquals(500000L, result.amountMinor)
        assertEquals(AccountHint.BANK, result.accountHint)
        assertEquals(AccountHint.CASH, result.counterAccountHint)
        assertEquals(ParseConfidence.LOW, result.baseConfidence)
    }

    @Test
    fun `a debit mentioning bKash becomes a transfer from bank to bKash`() {
        val body = "Your A/C XXXX1234 has been debited by Tk 2,000.00 on 22-09-2026 for transfer to bKash. Available Balance Tk 40,000.00"
        val result = parser.parse("MyBank", body, now)!!

        assertEquals(TransactionType.TRANSFER, result.type)
        assertEquals(AccountHint.BANK, result.accountHint)
        assertEquals(AccountHint.BKASH, result.counterAccountHint)
        assertEquals(200000L, result.amountMinor)
    }

    @Test
    fun `a plain POS debit becomes an expense with the merchant extracted`() {
        val body = "Your A/C XXXX1234 has been debited by Tk 1,250.00 on 22-09-2026 for POS purchase at AGORA DHANMONDI. Available Balance Tk 38,750.00"
        val result = parser.parse("MyBank", body, now)!!

        assertEquals(TransactionType.EXPENSE, result.type)
        assertEquals(AccountHint.BANK, result.accountHint)
        assertEquals(125000L, result.amountMinor)
        assertEquals("AGORA DHANMONDI", result.merchant)
    }

    @Test
    fun `a credit becomes income`() {
        val body = "Your A/C XXXX1234 has been credited with Tk 10,000.00 on 22-09-2026. Available Balance Tk 48,750.00"
        val result = parser.parse("MyBank", body, now)!!

        assertEquals(TransactionType.INCOME, result.type)
        assertEquals(1000000L, result.amountMinor)
    }

    @Test
    fun `every generic bank parse is low confidence, never high`() {
        val body = "Your A/C XXXX1234 has been credited with Tk 10,000.00 on 22-09-2026."
        assertEquals(ParseConfidence.LOW, parser.parse("MyBank", body, now)!!.baseConfidence)
    }

    @Test
    fun `an unrelated message with no recognized keyword returns null`() {
        assertNull(parser.parse("MyBank", "Your statement is now available online.", now))
    }
}
