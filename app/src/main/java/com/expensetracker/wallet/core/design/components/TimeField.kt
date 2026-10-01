package com.expensetracker.wallet.core.design.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** A read-only text field that opens [GlassTimePickerDialog] on tap, preserving [dateMillis]'s
 * own date — only the hour/minute change. Pairs with [DateField], which does the reverse. */
@Composable
fun TimeField(dateMillis: Long, onTimeChange: (Long) -> Unit, modifier: Modifier = Modifier, label: String = "Time") {
    var showPicker by remember { mutableStateOf(false) }
    val formatted = remember(dateMillis) {
        SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(dateMillis))
    }

    Box(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = formatted,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth(),
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { showPicker = true },
        )
    }

    if (showPicker) {
        GlassTimePickerDialog(
            initialDateMillis = dateMillis,
            onDismiss = { showPicker = false },
            onConfirm = onTimeChange,
        )
    }
}
