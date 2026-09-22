package com.example.wallet.core.design.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.wallet.core.common.ReportRangePreset

/** A horizontally-scrolling row of preset date-range chips driving every report section below it
 * (plan.md §24) — presets rather than a full calendar range picker, since "this month / last
 * month / trailing N months" covers every report this app shows. */
@Composable
fun DateRangeSelector(
    selected: ReportRangePreset,
    onSelect: (ReportRangePreset) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(ReportRangePreset.entries.toList()) { preset ->
            FilterChip(
                selected = preset == selected,
                onClick = { onSelect(preset) },
                label = { Text(preset.label) },
            )
        }
    }
}
