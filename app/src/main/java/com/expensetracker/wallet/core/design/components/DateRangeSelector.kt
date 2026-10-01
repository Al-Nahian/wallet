package com.expensetracker.wallet.core.design.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.expensetracker.wallet.core.common.DateRange
import com.expensetracker.wallet.core.common.ReportRangePreset

/** A horizontally-scrolling row of preset date-range chips driving every report section below it
 * (plan.md §24) — presets plus a "Custom" option that opens [GlassDateRangePickerDialog] to pick
 * an explicit start/end. */
@Composable
fun DateRangeSelector(
    selected: ReportRangePreset,
    onSelect: (ReportRangePreset) -> Unit,
    onCustomRangeSelected: (DateRange) -> Unit,
    modifier: Modifier = Modifier,
    customRangeLabel: String? = null,
    currentCustomRange: DateRange? = null,
) {
    var showRangePicker by remember { mutableStateOf(false) }

    LazyRow(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(ReportRangePreset.entries.toList()) { preset ->
            val isCustom = preset == ReportRangePreset.CUSTOM
            FilterChip(
                selected = preset == selected,
                onClick = {
                    if (isCustom) {
                        showRangePicker = true
                    } else {
                        onSelect(preset)
                    }
                },
                label = { Text(if (isCustom && preset == selected && customRangeLabel != null) customRangeLabel else preset.label) },
            )
        }
    }

    if (showRangePicker) {
        GlassDateRangePickerDialog(
            initialRange = currentCustomRange,
            onDismiss = { showRangePicker = false },
            onConfirm = onCustomRangeSelected,
        )
    }
}
