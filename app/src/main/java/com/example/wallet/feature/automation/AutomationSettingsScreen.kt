package com.example.wallet.feature.automation

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.wallet.core.design.components.GlassScreenTopBar
import com.example.wallet.core.design.components.SecondaryButton

/**
 * plan.md §59/§60. The permission rationale is shown inline, before the system prompt, per §59 —
 * never a bare permission dialog with no explanation. Turning capture off doesn't revoke the
 * already-granted OS permission (Android has no API for an app to do that to itself); it just
 * stops `SmsReceiver` from acting on anything, checked in `ProcessIncomingSmsUseCase`.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutomationSettingsScreen(
    onBack: () -> Unit,
    onOpenReviewQueue: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AutomationSettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val requestSmsPermission = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            viewModel.setSmsCaptureEnabled(true)
        } else {
            Toast.makeText(context, "SMS permission was denied — capture stays off.", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            GlassScreenTopBar(
                title = "Automation",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = "SMS transaction detection", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "Reads bank and mobile-wallet (bKash, Nagad, Rocket) SMS on this " +
                        "device to record transactions automatically — nothing is sent " +
                        "anywhere. You can turn this off at any time.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(text = "Enable SMS detection", style = MaterialTheme.typography.bodyLarge)
                    Switch(
                        checked = uiState.smsCaptureEnabled,
                        onCheckedChange = { enabled ->
                            if (!enabled) {
                                viewModel.setSmsCaptureEnabled(false)
                                return@Switch
                            }
                            val alreadyGranted = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.RECEIVE_SMS,
                            ) == PackageManager.PERMISSION_GRANTED
                            if (alreadyGranted) {
                                viewModel.setSmsCaptureEnabled(true)
                            } else {
                                requestSmsPermission.launch(Manifest.permission.RECEIVE_SMS)
                            }
                        },
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = "Automatic add", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "When on, a clean, high-confidence match (like a bKash payment) is " +
                        "added straight to your ledger and you're notified with an undo. " +
                        "When off, every detected transaction waits in the Review Queue for " +
                        "you to confirm first.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(text = "Automatically add high-confidence transactions", style = MaterialTheme.typography.bodyLarge)
                    Switch(
                        checked = uiState.automaticAddEnabled,
                        enabled = uiState.smsCaptureEnabled,
                        onCheckedChange = viewModel::setAutomaticAddEnabled,
                    )
                }
            }

            SecondaryButton(
                text = if (uiState.pendingReviewCount > 0) "Review Queue (${uiState.pendingReviewCount})" else "Review Queue",
                onClick = onOpenReviewQueue,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
