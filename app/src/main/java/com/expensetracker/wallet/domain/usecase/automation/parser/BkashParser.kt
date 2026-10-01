package com.expensetracker.wallet.domain.usecase.automation.parser

import com.expensetracker.wallet.core.common.parseMoneyToMinorUnits
import com.expensetracker.wallet.domain.model.ParseConfidence
import com.expensetracker.wallet.domain.model.TransactionType
import javax.inject.Inject

private val AMOUNT = """Tk\.?\s?([\d,]+(?:\.\d{1,2})?)"""
private val TRX_ID = Regex("""TrxID\s+(\w+)""", RegexOption.IGNORE_CASE)

private val PAYMENT = Regex("""Payment\s+$AMOUNT\s+to\s+(.+?)\s+successful""", RegexOption.IGNORE_CASE)
private val CASH_OUT = Regex("""Cash Out\s+$AMOUNT\s+from agent""", RegexOption.IGNORE_CASE)
private val RECEIVED = Regex("""(?:you have )?received\s+$AMOUNT\s+from\s+([^\s.]+)""", RegexOption.IGNORE_CASE)
private val SENT = Regex("""(?:you have )?sent\s+$AMOUNT\s+to\s+([^\s.]+)""", RegexOption.IGNORE_CASE)
private val ADD_MONEY = Regex("""$AMOUNT\s+added to your bKash account""", RegexOption.IGNORE_CASE)

/**
 * plan.md §32 — bKash's own SMS templates (publicly documented wording; the exact phrasing can
 * drift over time, which is exactly what [ParsedTransaction.baseConfidence] = HIGH assumes the
 * risk of — a template change makes [parse] return null, not a wrong guess, per the fail-closed
 * rule). Covers the four everyday flows plus the explicit transfer cases the user asked for:
 * a merchant Payment, a Cash Out (bKash -> Cash, plan.md §88-adjacent transfer detection), Send
 * Money in both directions, and Add Money (Bank -> bKash).
 */
class BkashParser @Inject constructor() : SmsParser {
    override val providerName: String = "bKash"

    override fun canHandle(sender: String, body: String): Boolean = sender.contains("bkash", ignoreCase = true)

    override fun parse(sender: String, body: String, receivedAt: Long): ParsedTransaction? {
        val trxId = TRX_ID.find(body)?.groupValues?.get(1)

        PAYMENT.find(body)?.let { match ->
            val amount = toMinorUnits(match.groupValues[1]) ?: return null
            return ParsedTransaction(
                type = TransactionType.EXPENSE,
                amountMinor = amount,
                currency = "BDT",
                accountHint = AccountHint.BKASH,
                merchant = match.groupValues[2].trim(),
                referenceId = trxId,
                occurredAt = receivedAt,
                provider = providerName,
                baseConfidence = ParseConfidence.HIGH,
            )
        }

        CASH_OUT.find(body)?.let { match ->
            val amount = toMinorUnits(match.groupValues[1]) ?: return null
            return ParsedTransaction(
                type = TransactionType.TRANSFER,
                amountMinor = amount,
                currency = "BDT",
                accountHint = AccountHint.BKASH,
                counterAccountHint = AccountHint.CASH,
                merchant = "Cash Out",
                referenceId = trxId,
                occurredAt = receivedAt,
                provider = providerName,
                baseConfidence = ParseConfidence.HIGH,
            )
        }

        ADD_MONEY.find(body)?.let { match ->
            val amount = toMinorUnits(match.groupValues[1]) ?: return null
            return ParsedTransaction(
                type = TransactionType.TRANSFER,
                amountMinor = amount,
                currency = "BDT",
                accountHint = AccountHint.BANK,
                counterAccountHint = AccountHint.BKASH,
                merchant = "Add Money",
                referenceId = trxId,
                occurredAt = receivedAt,
                provider = providerName,
                baseConfidence = ParseConfidence.HIGH,
            )
        }

        RECEIVED.find(body)?.let { match ->
            val amount = toMinorUnits(match.groupValues[1]) ?: return null
            return ParsedTransaction(
                type = TransactionType.INCOME,
                amountMinor = amount,
                currency = "BDT",
                accountHint = AccountHint.BKASH,
                merchant = match.groupValues[2].trim(),
                referenceId = trxId,
                occurredAt = receivedAt,
                provider = providerName,
                baseConfidence = ParseConfidence.HIGH,
            )
        }

        SENT.find(body)?.let { match ->
            val amount = toMinorUnits(match.groupValues[1]) ?: return null
            return ParsedTransaction(
                type = TransactionType.EXPENSE,
                amountMinor = amount,
                currency = "BDT",
                accountHint = AccountHint.BKASH,
                merchant = match.groupValues[2].trim(),
                referenceId = trxId,
                occurredAt = receivedAt,
                provider = providerName,
                baseConfidence = ParseConfidence.HIGH,
            )
        }

        return null
    }

    private fun toMinorUnits(raw: String): Long? = parseMoneyToMinorUnits(raw.replace(",", ""))
}
