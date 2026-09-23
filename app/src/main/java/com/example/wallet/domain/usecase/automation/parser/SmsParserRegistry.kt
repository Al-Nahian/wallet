package com.example.wallet.domain.usecase.automation.parser

import javax.inject.Inject

/** plan.md §32 — tries every registered [SmsParser] (order doesn't matter; each provider's
 * [SmsParser.canHandle] should be mutually exclusive) and returns the first match, or null if no
 * parser recognized the message at all (the fail-closed "ignore, don't guess" path). */
class SmsParserRegistry @Inject constructor(
    private val parsers: Set<@JvmSuppressWildcards SmsParser>,
) {
    fun parse(sender: String, body: String, receivedAt: Long): ParsedTransaction? =
        parsers.firstOrNull { it.canHandle(sender, body) }?.parse(sender, body, receivedAt)
}
