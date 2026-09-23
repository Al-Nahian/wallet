package com.example.wallet.core.design.components

import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.wallet.core.design.glass.applyDialogBlurBehind
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/** A read-only text field that opens the platform time picker on tap, preserving [dateMillis]'s
 * own date — only the hour/minute change. Pairs with [DateField], which does the reverse. */
@Composable
fun TimeField(dateMillis: Long, onTimeChange: (Long) -> Unit, modifier: Modifier = Modifier, label: String = "Time") {
    val context = LocalContext.current
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
                .clickable {
                    val calendar = Calendar.getInstance().apply { timeInMillis = dateMillis }
                    TimePickerDialog(
                        context,
                        { _, hourOfDay, minute ->
                            val picked = Calendar.getInstance().apply {
                                timeInMillis = dateMillis
                                set(Calendar.HOUR_OF_DAY, hourOfDay)
                                set(Calendar.MINUTE, minute)
                                set(Calendar.SECOND, 0)
                                set(Calendar.MILLISECOND, 0)
                            }
                            onTimeChange(picked.timeInMillis)
                        },
                        calendar.get(Calendar.HOUR_OF_DAY),
                        calendar.get(Calendar.MINUTE),
                        false,
                    ).apply { window?.let { applyDialogBlurBehind(it) } }.show()
                },
        )
    }
}
