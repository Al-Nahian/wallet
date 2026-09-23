package com.example.wallet.core.automation

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.example.wallet.domain.usecase.automation.ProcessIncomingSmsUseCase
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * plan.md §30 — only ever receives anything once the user has both opted into SMS capture
 * (Automation Settings) *and* granted `RECEIVE_SMS` at runtime; the manifest registration alone
 * grants no access. `goAsync()` extends the receiver's lifetime past `onReceive` returning so the
 * (async, Hilt-injected) parse-and-persist work in [ProcessIncomingSmsUseCase] can actually
 * finish — a plain `onReceive` is killed by the OS the moment it returns. Never logs the raw
 * message; [processIncomingSms] only ever sees it in memory for the duration of one parse.
 */
@AndroidEntryPoint
class SmsReceiver : BroadcastReceiver() {

    @Inject
    lateinit var processIncomingSms: ProcessIncomingSmsUseCase

    private val receiverScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        val sender = messages.firstOrNull()?.originatingAddress ?: return
        val body = messages.joinToString(separator = "") { it.messageBody.orEmpty() }
        val receivedAt = messages.firstOrNull()?.timestampMillis ?: System.currentTimeMillis()

        val pendingResult = goAsync()
        receiverScope.launch {
            try {
                processIncomingSms(sender, body, receivedAt)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
