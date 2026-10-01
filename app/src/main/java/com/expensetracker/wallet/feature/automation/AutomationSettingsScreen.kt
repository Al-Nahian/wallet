package com.expensetracker.wallet.feature.automation

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import com.expensetracker.wallet.core.design.components.GlassScreenScaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.expensetracker.wallet.core.design.WalletTheme
import com.expensetracker.wallet.core.design.components.GlassScreenTopBar
import com.expensetracker.wallet.core.design.components.LiquidGlassCard
import com.expensetracker.wallet.core.design.glass.GlassColors
import com.expensetracker.wallet.core.design.glass.GlassShapes

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

    GlassScreenScaffold(
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SettingCard(
                title = "SMS transaction detection",
                description = "Reads bank and mobile-wallet (bKash, Nagad, Rocket) SMS on this " +
                    "device to record transactions automatically — nothing is sent " +
                    "anywhere. You can turn this off at any time.",
                switchLabel = "Enable SMS detection",
                checked = uiState.smsCaptureEnabled,
                onCheckedChange = { enabled ->
                    if (!enabled) {
                        viewModel.setSmsCaptureEnabled(false)
                        return@SettingCard
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

            SettingCard(
                title = "Automatic add",
                description = "When on, a clean, high-confidence match (like a bKash payment) " +
                    "is added straight to your ledger and you're notified with an undo. When " +
                    "off, every detected transaction waits in the Review Queue for you to " +
                    "confirm first.",
                switchLabel = "Automatically add high-confidence transactions",
                checked = uiState.automaticAddEnabled,
                switchEnabled = uiState.smsCaptureEnabled,
                onCheckedChange = viewModel::setAutomaticAddEnabled,
            )

            ReviewQueueRow(pendingCount = uiState.pendingReviewCount, onClick = onOpenReviewQueue)
        }
    }
}

@Composable
private fun SettingCard(
    title: String,
    description: String,
    switchLabel: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    switchEnabled: Boolean = true,
) {
    LiquidGlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = GlassShapes.medium,
        tint = GlassColors.neutralGlassTint(WalletTheme.extendedColors.transfer),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = switchLabel,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f).padding(end = 12.dp),
                )
                Switch(checked = checked, enabled = switchEnabled, onCheckedChange = onCheckedChange)
            }
        }
    }
}

@Composable
private fun ReviewQueueRow(pendingCount: Int, onClick: () -> Unit) {
    LiquidGlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = GlassShapes.medium,
        tint = GlassColors.neutralGlassTint(WalletTheme.extendedColors.transfer),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Filled.Sms, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                text = "Review Queue",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f).padding(start = 12.dp),
            )
            if (pendingCount > 0) {
                Text(
                    text = pendingCount.toString(),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(end = 8.dp),
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(2.dp),
            )
        }
    }
}
