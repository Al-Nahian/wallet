package com.expensetracker.wallet.domain.usecase.recurring

import com.expensetracker.wallet.domain.model.RecurringFrequency
import java.util.Calendar

/**
 * plans/11-recurring-goals.md — advances a recurring rule's `nextDate` by one occurrence of
 * [RecurringFrequency]. The only subtlety is MONTHLY/YEARLY: `Calendar.add` does not clamp an
 * overflowing day-of-month, it *rolls over* (Jan 31 + 1 month becomes Mar 3, not Feb 28 — it
 * adds 31 days'-worth of month, then lets the day spill into March). That's wrong for billing
 * dates, so month/year steps here explicitly clamp the day back to the target month's last valid
 * day (e.g. Jan 31 -> Feb 28/29 -> Mar 28 -> Apr 28 -> ... -> Dec 28 -> Jan 28). Note this means
 * the anchor day drifts down permanently after the first short month rather than snapping back
 * to 31 on a 31-day month — there's no separate "anchor day" field on the entity to restore from,
 * only `nextDate` itself, so the rule's own last date is the only source of truth.
 */
object RecurringSchedule {
    fun nextOccurrence(currentDate: Long, frequency: RecurringFrequency): Long {
        val calendar = Calendar.getInstance().apply { timeInMillis = currentDate }
        when (frequency) {
            RecurringFrequency.DAILY -> calendar.add(Calendar.DAY_OF_MONTH, 1)
            RecurringFrequency.WEEKLY -> calendar.add(Calendar.WEEK_OF_YEAR, 1)
            RecurringFrequency.MONTHLY -> calendar.stepByClampedMonths(months = 1)
            RecurringFrequency.YEARLY -> calendar.stepByClampedMonths(months = 12)
        }
        return calendar.timeInMillis
    }

    /** Adds [months] without the day-of-month rollover `Calendar.add(MONTH, ...)` has: move to
     * the 1st first (so the add itself can never overflow), then clamp the original day back
     * onto whatever the destination month's last day is. */
    private fun Calendar.stepByClampedMonths(months: Int) {
        val originalDay = get(Calendar.DAY_OF_MONTH)
        set(Calendar.DAY_OF_MONTH, 1)
        add(Calendar.MONTH, months)
        val lastDayOfTargetMonth = getActualMaximum(Calendar.DAY_OF_MONTH)
        set(Calendar.DAY_OF_MONTH, minOf(originalDay, lastDayOfTargetMonth))
    }
}
