package com.example.wallet.core.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.wallet.domain.usecase.recurring.GenerateDueRecurringTransactionsUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * plans/11-recurring-goals.md — runs [GenerateDueRecurringTransactionsUseCase] once a day
 * (scheduled from `WalletApplication`). Kept as a thin wrapper: all the actual logic — deciding
 * auto-post vs. reminder, advancing `nextDate`, firing notifications — lives in the use case so
 * it can be unit-tested without touching WorkManager at all.
 */
@HiltWorker
class RecurringTransactionWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val generateDueRecurringTransactions: GenerateDueRecurringTransactionsUseCase,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            generateDueRecurringTransactions()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
