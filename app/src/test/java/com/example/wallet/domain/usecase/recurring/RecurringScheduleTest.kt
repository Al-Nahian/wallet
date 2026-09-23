package com.example.wallet.domain.usecase.recurring

import com.example.wallet.domain.model.RecurringFrequency
import java.util.Calendar
import org.junit.Assert.assertEquals
import org.junit.Test

private fun dateOf(year: Int, month: Int, day: Int): Long =
    Calendar.getInstance().apply {
        set(year, month - 1, day, 0, 0, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

private fun Long.toYmd(): Triple<Int, Int, Int> =
    Calendar.getInstance().apply { timeInMillis = this@toYmd }.let {
        Triple(it.get(Calendar.YEAR), it.get(Calendar.MONTH) + 1, it.get(Calendar.DAY_OF_MONTH))
    }

class RecurringScheduleTest {

    @Test
    fun `daily advances by exactly one day`() {
        val next = RecurringSchedule.nextOccurrence(dateOf(2025, 3, 10), RecurringFrequency.DAILY)
        assertEquals(Triple(2025, 3, 11), next.toYmd())
    }

    @Test
    fun `daily rolls over the month boundary`() {
        val next = RecurringSchedule.nextOccurrence(dateOf(2025, 1, 31), RecurringFrequency.DAILY)
        assertEquals(Triple(2025, 2, 1), next.toYmd())
    }

    @Test
    fun `weekly advances by exactly seven days`() {
        val next = RecurringSchedule.nextOccurrence(dateOf(2025, 3, 10), RecurringFrequency.WEEKLY)
        assertEquals(Triple(2025, 3, 17), next.toYmd())
    }

    @Test
    fun `monthly keeps the same day-of-month in a normal month`() {
        val next = RecurringSchedule.nextOccurrence(dateOf(2025, 3, 15), RecurringFrequency.MONTHLY)
        assertEquals(Triple(2025, 4, 15), next.toYmd())
    }

    @Test
    fun `monthly from the 31st clamps to February's last day, not March 3rd`() {
        // The bug this whole class exists to avoid: java.util.Calendar.add(MONTH, 1) on Jan 31
        // rolls over to Mar 3 (28 days into Feb, then 3 more), not Feb 28. RecurringSchedule
        // must clamp to the target month's actual last day instead.
        val next = RecurringSchedule.nextOccurrence(dateOf(2025, 1, 31), RecurringFrequency.MONTHLY)
        assertEquals(Triple(2025, 2, 28), next.toYmd())
    }

    @Test
    fun `monthly from the 31st clamps to Feb 29 in a leap year`() {
        val next = RecurringSchedule.nextOccurrence(dateOf(2024, 1, 31), RecurringFrequency.MONTHLY)
        assertEquals(Triple(2024, 2, 29), next.toYmd())
    }

    @Test
    fun `monthly clamped once at Feb stays clamped stepping into March`() {
        // Documented trade-off (see RecurringSchedule's kdoc): the anchor day drifts down
        // permanently after the first short month, since nextDate is the only stored anchor.
        val clampedToFeb28 = dateOf(2025, 2, 28)
        val next = RecurringSchedule.nextOccurrence(clampedToFeb28, RecurringFrequency.MONTHLY)
        assertEquals(Triple(2025, 3, 28), next.toYmd())
    }

    @Test
    fun `monthly from month-end 30th lands on April 30th, not May 1st`() {
        val next = RecurringSchedule.nextOccurrence(dateOf(2025, 4, 30), RecurringFrequency.MONTHLY)
        assertEquals(Triple(2025, 5, 30), next.toYmd())
    }

    @Test
    fun `yearly keeps month and day in a normal year`() {
        val next = RecurringSchedule.nextOccurrence(dateOf(2025, 6, 15), RecurringFrequency.YEARLY)
        assertEquals(Triple(2026, 6, 15), next.toYmd())
    }

    @Test
    fun `yearly from Feb 29 on a leap year clamps to Feb 28 the next year`() {
        val next = RecurringSchedule.nextOccurrence(dateOf(2024, 2, 29), RecurringFrequency.YEARLY)
        assertEquals(Triple(2025, 2, 28), next.toYmd())
    }
}
