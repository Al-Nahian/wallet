package com.expensetracker.wallet.domain.usecase.automation.parser

import com.expensetracker.wallet.core.common.parseMoneyToMinorUnits
import com.expensetracker.wallet.domain.model.ParseConfidence
import com.expensetracker.wallet.domain.model.TransactionType
import javax.inject.Inject

private val AMOUNT = Regex("""(?:Tk\.?|BDT)\s?([\d,]+(?:\.\d{1,2})?)""", RegexOption.IGNORE_CASE)
private val REFERENCE = Regex("""(?:Ref(?:erence)?\.?\s*(?:No\.?|ID)?[:\s]+)(\S+)""", RegexOption.IGNORE_CASE)
private val MERCHANT_AT = Regex("""\bat\s+([A-Za-z0-9 &'\-.]{2,40}?)(?:\s+on\b|\.|,|$)""", RegexOption.IGNORE_CASE)

/**
 * plan.md §32's "start with 1-2 real providers, stub the rest" fallback — a best-effort, keyword
 * + flexible-amount-regex parser that isn't tied to any one Bangladeshi bank's exact template,
 * since template wording varies a lot bank to bank and no real sample messages were available to
 * verify one precisely. Every result is deliberately [ParseConfidence.LOW] regardless of how
 * clean the individual parse looks — unlike [BkashParser]'s well-established, publicly stable
 * template, this parser's own correctness hasn't been verified against a real bank's actual SMS,
 * so it always lands in the Review Queue rather than auto-adding. Swap in (or add alongside) a
 * verified parser for a specific bank, with real anonymized sample messages per plan.md §53, to
 * safely upgrade that bank's own confidence to HIGH.
 *
 * Covers exactly the transfer cases the feature request called out: an ATM withdrawal is
 * `Bank -> Cash`, and a bank SMS that also mentions bKash (a debit alongside "bkash") is
 * `Bank -> bKash`. A plain debit becomes an `EXPENSE` with the merchant (if extractable) as the
 * payee/note; a plain credit becomes an `INCOME`.
 */
class GenericBankParser @Inject constructor() : SmsParser {
    override val providerName: String = "GenericBank"

    override fun canHandle(sender: String, body: String): Boolean {
        val lower = body.lowercase()
        return listOf("debited", "credited", "withdrawn", "withdrawal").any { it in lower }
    }

    override fun parse(sender: String, body: String, receivedAt: Long): ParsedTransaction? {
        val lower = body.lowercase()
        val amount = AMOUNT.find(body)?.groupValues?.get(1)?.let(::toMinorUnits) ?: return null
        val referenceId = REFERENCE.find(body)?.groupValues?.get(1)

        val isAtm = "atm" in lower && ("withdrawn" in lower || "withdrawal" in lower)
        if (isAtm) {
            return ParsedTransaction(
                type = TransactionType.TRANSFER,
                amountMinor = amount,
                currency = "BDT",
                accountHint = AccountHint.BANK,
                counterAccountHint = AccountHint.CASH,
                merchant = "ATM Withdrawal",
                referenceId = referenceId,
                occurredAt = receivedAt,
                provider = providerName,
                baseConfidence = ParseConfidence.LOW,
            )
        }

        val isDebit = "debited" in lower
        val mentionsBkash = "bkash" in lower
        if (isDebit && mentionsBkash) {
            return ParsedTransaction(
                type = TransactionType.TRANSFER,
                amountMinor = amount,
                currency = "BDT",
                accountHint = AccountHint.BANK,
                counterAccountHint = AccountHint.BKASH,
                merchant = "Transfer to bKash",
                referenceId = referenceId,
                occurredAt = receivedAt,
                provider = providerName,
                baseConfidence = ParseConfidence.LOW,
            )
        }

        if (isDebit) {
            val merchant = MERCHANT_AT.find(body)?.groupValues?.get(1)?.trim()
            return ParsedTransaction(
                type = TransactionType.EXPENSE,
                amountMinor = amount,
                currency = "BDT",
                accountHint = AccountHint.BANK,
                merchant = merchant,
                referenceId = referenceId,
                occurredAt = receivedAt,
                provider = providerName,
                baseConfidence = ParseConfidence.LOW,
            )
        }

        if ("credited" in lower) {
            return ParsedTransaction(
                type = TransactionType.INCOME,
                amountMinor = amount,
                currency = "BDT",
                accountHint = AccountHint.BANK,
                merchant = MERCHANT_AT.find(body)?.groupValues?.get(1)?.trim(),
                referenceId = referenceId,
                occurredAt = receivedAt,
                provider = providerName,
                baseConfidence = ParseConfidence.LOW,
            )
        }

        return null
    }

    private fun toMinorUnits(raw: String): Long? = parseMoneyToMinorUnits(raw.replace(",", ""))
}
