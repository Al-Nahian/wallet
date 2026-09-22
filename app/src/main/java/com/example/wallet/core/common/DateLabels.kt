package com.example.wallet.core.common

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Groups the transactions list by calendar day (plan.md §23). Uses `Calendar`/`SimpleDateFormat`
 * rather than `java.time` since minSdk 24 doesn't have it without core-library desugaring, which
 * isn't enabled.
 */
fun dateGroupLabel(epochMillis: Long, now: Long = System.currentTimeMillis()): String {
    val target = Calendar.getInstance().apply { timeInMillis = epochMillis }
    val today = Calendar.getInstance().apply { timeInMillis = now }
    val yesterday = Calendar.getInstance().apply {
        timeInMillis = now
        add(Calendar.DAY_OF_YEAR, -1)
    }

    return when {
        isSameDay(target, today) -> "Today"
        isSameDay(target, yesterday) -> "Yesterday"
        else -> SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(epochMillis))
    }
}

private fun isSameDay(a: Calendar, b: Calendar): Boolean =
    a.get(Calendar.YEAR) == b.get(Calendar.YEAR) && a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR)

/** plan.md §20/§25 — the dashboard's "this month" window: midnight on the 1st through [now]. */
fun startOfCurrentMonthMillis(now: Long = System.currentTimeMillis()): Long =
    Calendar.getInstance().apply {
        timeInMillis = now
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

/** Day-of-month for [now] (1-31) — the denominator for average daily spend. */
fun currentDayOfMonth(now: Long = System.currentTimeMillis()): Int =
    Calendar.getInstance().apply { timeInMillis = now }.get(Calendar.DAY_OF_MONTH)
