package com.example.wallet.core.common

import java.util.Calendar

/** An inclusive [startInclusive, endInclusive] window in epoch millis. */
data class DateRange(val startInclusive: Long, val endInclusive: Long)

/** plan.md §24 — the reports screen's shared date-range presets. */
enum class ReportRangePreset(val label: String) {
    THIS_MONTH("This Month"),
    LAST_MONTH("Last Month"),
    LAST_3_MONTHS("Last 3 Months"),
    LAST_6_MONTHS("Last 6 Months"),
    THIS_YEAR("This Year"),
    CUSTOM("Custom"),
}

/** Resolves a preset to a concrete range against [now], so every report section recomputes
 * consistently when the user changes the selector. [customRange] backs [ReportRangePreset.CUSTOM]
 * — the only preset with no fixed formula, since it comes from the user's own date-range pick;
 * it falls back to "this month" if a caller ever hits it before one has been chosen. */
fun dateRangeForPreset(preset: ReportRangePreset, now: Long = System.currentTimeMillis(), customRange: DateRange? = null): DateRange =
    when (preset) {
        ReportRangePreset.THIS_MONTH -> DateRange(startOfCurrentMonthMillis(now), now)
        ReportRangePreset.LAST_MONTH -> monthRange(1, now)
        ReportRangePreset.LAST_3_MONTHS -> DateRange(monthsAgoStart(2, now), now)
        ReportRangePreset.LAST_6_MONTHS -> DateRange(monthsAgoStart(5, now), now)
        ReportRangePreset.THIS_YEAR -> {
            val start = Calendar.getInstance().apply {
                timeInMillis = now
                set(Calendar.DAY_OF_YEAR, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            DateRange(start.timeInMillis, now)
        }
        ReportRangePreset.CUSTOM -> customRange ?: DateRange(startOfCurrentMonthMillis(now), now)
    }

private fun monthsAgoStart(monthsBack: Int, now: Long): Long =
    Calendar.getInstance().apply {
        timeInMillis = now
        add(Calendar.MONTH, -monthsBack)
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

/** The full calendar month [monthsAgo] months before [now]'s month (0 = current month) — used to
 * build the category trend comparison's per-month buckets (current, previous, trailing 3/6 avg). */
fun monthRange(monthsAgo: Int, now: Long = System.currentTimeMillis()): DateRange {
    val start = Calendar.getInstance().apply {
        timeInMillis = now
        add(Calendar.MONTH, -monthsAgo)
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val end = Calendar.getInstance().apply {
        timeInMillis = start.timeInMillis
        set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
        set(Calendar.MILLISECOND, 999)
    }
    return DateRange(start.timeInMillis, minOf(end.timeInMillis, now))
}
