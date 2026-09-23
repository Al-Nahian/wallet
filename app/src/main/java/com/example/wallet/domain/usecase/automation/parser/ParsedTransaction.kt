package com.example.wallet.domain.usecase.automation.parser

import com.example.wallet.domain.model.ParseConfidence
import com.example.wallet.domain.model.TransactionType

/**
 * plan.md §32 — a well-known money-holding concept an SMS mentions, resolved to a real
 * [com.example.wallet.domain.model.Account] later (see `ResolveAccountHintUseCase`). `CASH` and
 * the MFS wallets are singular, well-known concepts that are safe to auto-create if the user
 * doesn't have one yet; `BANK` is deliberately generic (which bank isn't knowable from most
 * messages) and is only ever auto-*matched*, never auto-*created*.
 */
enum class AccountHint {
    CASH,
    BKASH,
    NAGAD,
    ROCKET,
    BANK,
}

/**
 * The result of parsing one SMS/notification (plan.md §32). For `type == TRANSFER`,
 * [accountHint] is the money's source and [counterAccountHint] its destination — e.g. an ATM
 * withdrawal is `accountHint = BANK, counterAccountHint = CASH`; a bKash cash-out is
 * `accountHint = BKASH, counterAccountHint = CASH`. For `EXPENSE`/`INCOME`, only [accountHint]
 * (the account the money moved through) is set. [referenceId] is the provider's own transaction
 * ID when the message includes one — the strongest possible dedup key (plan.md §33); when absent,
 * the caller falls back to a hash of sender+body+timestamp.
 */
data class ParsedTransaction(
    val type: TransactionType,
    val amountMinor: Long,
    val currency: String,
    val accountHint: AccountHint,
    val counterAccountHint: AccountHint? = null,
    val merchant: String? = null,
    val referenceId: String? = null,
    val occurredAt: Long,
    val provider: String,
    /** The parser's own confidence in this specific parse, before account-resolution ambiguity
     * (checked separately by the caller) can only ever downgrade it further, never upgrade it. */
    val baseConfidence: ParseConfidence,
)
