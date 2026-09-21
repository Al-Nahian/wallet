package com.example.wallet.core.common

/**
 * Money is always integer minor units (plan.md §12/§26 rule 6) — these are the only two
 * places a user-facing decimal string should ever be parsed from or formatted to. Assumes
 * 2 decimal places for every currency, which holds for BDT (the only currency in scope per
 * §27's MVP scope); revisit if/when a zero-decimal currency (e.g. JPY) is ever supported.
 */
private val MONEY_INPUT_REGEX = Regex("""-?\d+(\.\d{1,2})?""")

fun parseMoneyToMinorUnits(input: String): Long? {
    val cleaned = input.trim().replace(",", "")
    if (cleaned.isEmpty() || !MONEY_INPUT_REGEX.matches(cleaned)) return null

    val negative = cleaned.startsWith("-")
    val unsigned = cleaned.removePrefix("-")
    val parts = unsigned.split(".")
    val wholePart = parts[0].toLongOrNull() ?: return null
    val fractionPart = if (parts.size > 1) parts[1].padEnd(2, '0').toLong() else 0L

    val minor = wholePart * 100 + fractionPart
    return if (negative) -minor else minor
}

/** Minor units -> an editable decimal string (e.g. `100050L` -> `"1000.50"`), for prefilling a
 * form field in edit mode. The inverse of [parseMoneyToMinorUnits]; avoids floating point. */
fun minorUnitsToEditableString(amountMinor: Long): String {
    val negative = amountMinor < 0
    val absMinor = kotlin.math.abs(amountMinor)
    val whole = absMinor / 100
    val fraction = absMinor % 100
    val sign = if (negative) "-" else ""
    return "$sign$whole.${fraction.toString().padStart(2, '0')}"
}

fun formatMoney(amountMinor: Long, currency: String): String {
    val negative = amountMinor < 0
    val absMinor = kotlin.math.abs(amountMinor)
    val whole = absMinor / 100
    val fraction = absMinor % 100

    val groupedWhole = whole.toString().reversed().chunked(3).joinToString(",").reversed()
    val sign = if (negative) "-" else ""
    return "$currency $sign$groupedWhole.${fraction.toString().padStart(2, '0')}"
}
