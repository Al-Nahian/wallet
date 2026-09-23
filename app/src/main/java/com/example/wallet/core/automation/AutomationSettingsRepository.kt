package com.example.wallet.core.automation

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.automationDataStore by preferencesDataStore(name = "automation_settings")

/**
 * plan.md §59/§60 — SMS/notification capture is opt-in only, never requested or enabled at first
 * launch. [isSmsCaptureEnabled] gates both the runtime permission request and whether
 * `SmsReceiver` does anything at all; [isAutomaticAddEnabled] is the §87 two-tier toggle —
 * turning it off routes even high-confidence parses through the Review Queue instead of
 * auto-adding, while capture (and therefore low-confidence review candidates) keeps working.
 */
@Singleton
class AutomationSettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private object Keys {
        val SMS_CAPTURE_ENABLED = booleanPreferencesKey("sms_capture_enabled")
        val AUTOMATIC_ADD_ENABLED = booleanPreferencesKey("automatic_add_enabled")
    }

    val isSmsCaptureEnabled: Flow<Boolean> =
        context.automationDataStore.data.map { it[Keys.SMS_CAPTURE_ENABLED] ?: false }

    /** Defaults on (per the §87-revised policy) so the moment a user opts into capture at all,
     * clean high-confidence parses start auto-adding immediately rather than piling up in the
     * Review Queue until they find a second toggle. */
    val isAutomaticAddEnabled: Flow<Boolean> =
        context.automationDataStore.data.map { it[Keys.AUTOMATIC_ADD_ENABLED] ?: true }

    suspend fun setSmsCaptureEnabled(enabled: Boolean) {
        context.automationDataStore.edit { it[Keys.SMS_CAPTURE_ENABLED] = enabled }
    }

    suspend fun setAutomaticAddEnabled(enabled: Boolean) {
        context.automationDataStore.edit { it[Keys.AUTOMATIC_ADD_ENABLED] = enabled }
    }
}
