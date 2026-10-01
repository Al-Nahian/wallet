package com.expensetracker.wallet.core.work

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

private const val RECURRING_WORK_NAME = "recurring_transactions"

/** Enqueues [RecurringTransactionWorker] once, at app startup — `KEEP` means a rescheduling
 * app update or a repeat `onCreate` (e.g. process restart) never duplicates or resets the
 * already-scheduled job. A daily cadence is enough: the worker processes every rule whose
 * `nextDate` has arrived, not just ones due "today," so a missed/delayed run (device asleep,
 * battery saver, etc.) still catches up next time it fires — nothing is silently skipped. */
fun scheduleRecurringTransactionWork(context: Context) {
    val request = PeriodicWorkRequestBuilder<RecurringTransactionWorker>(1, TimeUnit.DAYS).build()
    WorkManager.getInstance(context)
        .enqueueUniquePeriodicWork(RECURRING_WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
}
