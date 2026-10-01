package com.expensetracker.wallet.core.design.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.expensetracker.wallet.core.design.glass.GlassStyle
import com.expensetracker.wallet.core.design.glass.GlassWindowBlur
import com.expensetracker.wallet.core.design.glass.glassDialogContainerColor

@Composable
fun ConfirmationDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    confirmLabel: String = "Confirm",
    dismissLabel: String = "Cancel",
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            GlassWindowBlur()
            Text(message)
        },
        confirmButton = { TextButton(onClick = onConfirm) { Text(confirmLabel) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(dismissLabel) } },
        containerColor = glassDialogContainerColor(GlassStyle.Thick),
    )
}
