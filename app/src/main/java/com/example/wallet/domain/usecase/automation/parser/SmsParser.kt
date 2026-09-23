package com.example.wallet.domain.usecase.automation.parser

/** plan.md §32 — one provider's SMS template knowledge, isolated behind this interface so the
 * capture pipeline (`ProcessIncomingSmsUseCase`) never depends on a specific provider's format. */
interface SmsParser {
    val providerName: String

    /** Cheap pre-check (usually just the sender ID/name) before attempting the more expensive
     * [parse] — lets [SmsParserRegistry] skip parsers that can't possibly match. */
    fun canHandle(sender: String, body: String): Boolean

    /** Returns null when [canHandle] was true but the message body didn't actually match any
     * known template (a changed template, a promotional message from the same sender, etc.) —
     * this is the "fail closed" path (plan.md §68): an unparseable message is silently ignored,
     * never a crash and never a guess. */
    fun parse(sender: String, body: String, receivedAt: Long): ParsedTransaction?
}
