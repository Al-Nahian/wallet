package com.example.wallet.core.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.wallet.core.common.DateRange
import com.example.wallet.core.design.glass.GlassStyle
import com.example.wallet.core.design.glass.GlassWindowBlur
import com.example.wallet.core.design.glass.glassDialogContainerColor
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlinx.coroutines.launch

/** Fixed compact width for every calendar pop-up — the old Compose M3 [androidx.compose.material3.DatePicker]
 * stretched to the full screen width once its dialog's platform-default-width constraint was
 * lifted (needed back then to stop it clipping its own rightmost day column). This custom
 * calendar has no such width requirement, so it can just declare the size it actually wants. */
private val CalendarWidth = 320.dp

/** A single day cell's diameter — [CalendarWidth] minus its own horizontal padding, split 7 ways. */
private val DayCellSize = 40.dp

private const val CalendarBaseYear = 1900
private const val CalendarPageCount = 12 * 400 // 1900..2300 — plenty either direction of "today".

private fun pageIndexFor(year: Int, month: Int): Int = (year - CalendarBaseYear) * 12 + month
private fun yearMonthForPage(page: Int): Pair<Int, Int> = (CalendarBaseYear + page / 12) to (page % 12)

private fun monthYearLabel(year: Int, month: Int): String {
    val cal = Calendar.getInstance().apply { clear(); set(year, month, 1) }
    return SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(cal.time)
}

/** Exactly as many full weeks as the month needs (4, 5, or 6) — not a fixed 42-cell/6-week grid,
 * which left a whole blank row's worth of empty space below short months' last week before the
 * Cancel/OK row. The dialog's height now varies a little across a swipe, which reads better than
 * that permanent gap. Null = blank leading or trailing cell. */
private fun monthGridDays(year: Int, month: Int): List<Int?> {
    val cal = Calendar.getInstance().apply { clear(); set(year, month, 1) }
    val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1 // 0 = Sunday
    val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val weeksNeeded = (firstDayOfWeek + daysInMonth + 6) / 7
    val cells = MutableList<Int?>(weeksNeeded * 7) { null }
    for (day in 1..daysInMonth) cells[firstDayOfWeek + day - 1] = day
    return cells
}

private fun localDayMillis(year: Int, month: Int, day: Int): Long =
    Calendar.getInstance().apply { clear(); set(year, month, day, 0, 0, 0) }.timeInMillis

/** The shared chrome every calendar pop-up uses: a prev/next month header (small type, unlike
 * the old M3 DatePicker's oversized headline) and a single fixed-size month grid — no
 * continuous-scroll list of months. [HorizontalPager] shows exactly one month at a time; swiping
 * (or the chevrons) pages to the next/previous one instead of scrolling through a long list. */
@Composable
private fun MonthPagerCalendar(
    initialYear: Int,
    initialMonth: Int,
    dayCell: @Composable (year: Int, month: Int, day: Int, onClick: () -> Unit) -> Unit,
    onDayClick: (year: Int, month: Int, day: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val pagerState = rememberPagerState(
        initialPage = remember { pageIndexFor(initialYear, initialMonth) },
        pageCount = { CalendarPageCount },
    )
    val scope = rememberCoroutineScope()
    val (headerYear, headerMonth) = yearMonthForPage(pagerState.currentPage)

    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            IconButton(onClick = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) } }) {
                Icon(Icons.Filled.ChevronLeft, contentDescription = "Previous month")
            }
            Text(
                text = monthYearLabel(headerYear, headerMonth),
                style = MaterialTheme.typography.titleSmall.copy(fontSize = 15.sp),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
            )
            IconButton(onClick = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } }) {
                Icon(Icons.Filled.ChevronRight, contentDescription = "Next month")
            }
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("S", "M", "T", "W", "T", "F", "S").forEach { label ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f),
                )
            }
        }
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxWidth()) { page ->
            val (year, month) = yearMonthForPage(page)
            Column {
                monthGridDays(year, month).chunked(7).forEach { week ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        week.forEach { day ->
                            Box(
                                modifier = Modifier.weight(1f).aspectRatio(1f),
                                contentAlignment = Alignment.Center,
                            ) {
                                if (day != null) dayCell(year, month, day) { onDayClick(year, month, day) }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SingleDayCell(selected: Boolean, label: Int, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(DayCellSize)
            .clip(CircleShape)
            .background(if (selected) MaterialTheme.colorScheme.primary else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label.toString(),
            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
        )
    }
}

/** A rounded-corner glass pop-up with a compact, single-month-at-a-time calendar — replaces the
 * platform [android.app.DatePickerDialog] (Material-2 chrome that clashed with the rest of the
 * liquid-glass UI) and, before that, Compose's own [androidx.compose.material3.DatePicker]
 * (full-width continuous month list with an oversized headline). Shared by every date field in
 * the app (transaction/budget/recurring forms). */
@Composable
fun GlassDatePickerDialog(initialDateMillis: Long, onDismiss: () -> Unit, onConfirm: (Long) -> Unit) {
    val initialCalendar = remember(initialDateMillis) { Calendar.getInstance().apply { timeInMillis = initialDateMillis } }
    var selectedYear by remember { mutableStateOf(initialCalendar.get(Calendar.YEAR)) }
    var selectedMonth by remember { mutableStateOf(initialCalendar.get(Calendar.MONTH)) }
    var selectedDay by remember { mutableStateOf(initialCalendar.get(Calendar.DAY_OF_MONTH)) }

    Dialog(onDismissRequest = onDismiss) {
        GlassWindowBlur()
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = glassDialogContainerColor(GlassStyle.Thick),
        ) {
            Column(modifier = Modifier.width(CalendarWidth).padding(16.dp)) {
                MonthPagerCalendar(
                    initialYear = selectedYear,
                    initialMonth = selectedMonth,
                    dayCell = { year, month, day, onClick ->
                        SingleDayCell(selected = year == selectedYear && month == selectedMonth && day == selectedDay, label = day, onClick = onClick)
                    },
                    onDayClick = { year, month, day ->
                        selectedYear = year
                        selectedMonth = month
                        selectedDay = day
                    },
                )
                Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    TextButton(
                        onClick = {
                            // Splice just the picked year/month/day onto initialDateMillis's own
                            // time-of-day rather than zeroing it out.
                            val picked = Calendar.getInstance().apply {
                                timeInMillis = initialDateMillis
                                set(selectedYear, selectedMonth, selectedDay)
                            }
                            onConfirm(picked.timeInMillis)
                            onDismiss()
                        },
                    ) { Text("OK") }
                }
            }
        }
    }
}

/** [GlassDatePickerDialog]'s time counterpart, wrapping Compose's [TimePicker] — unaffected by
 * the width/scroll issues above (it's already a compact, non-scrolling dial). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlassTimePickerDialog(initialDateMillis: Long, onDismiss: () -> Unit, onConfirm: (Long) -> Unit) {
    val initialCalendar = remember(initialDateMillis) { Calendar.getInstance().apply { timeInMillis = initialDateMillis } }
    val state = rememberTimePickerState(
        initialHour = initialCalendar.get(Calendar.HOUR_OF_DAY),
        initialMinute = initialCalendar.get(Calendar.MINUTE),
        is24Hour = false,
    )
    Dialog(onDismissRequest = onDismiss) {
        GlassWindowBlur()
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = glassDialogContainerColor(GlassStyle.Thick),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                TimePicker(state = state)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    TextButton(
                        onClick = {
                            val picked = Calendar.getInstance().apply {
                                timeInMillis = initialDateMillis
                                set(Calendar.HOUR_OF_DAY, state.hour)
                                set(Calendar.MINUTE, state.minute)
                                set(Calendar.SECOND, 0)
                                set(Calendar.MILLISECOND, 0)
                            }
                            onConfirm(picked.timeInMillis)
                            onDismiss()
                        },
                    ) { Text("OK") }
                }
            }
        }
    }
}

/** [GlassDatePickerDialog]'s range counterpart — used by the Reports screen's "Custom" date-range
 * option. First tap (or a tap after a range is already complete) starts a new range at that day;
 * a second tap either extends it as the end date, or — if it lands before the current start —
 * restarts the range there instead. */
@Composable
fun GlassDateRangePickerDialog(initialRange: DateRange?, onDismiss: () -> Unit, onConfirm: (DateRange) -> Unit) {
    val initialYear: Int
    val initialMonth: Int
    var rangeStartMillis by remember { mutableStateOf(initialRange?.startInclusive) }
    var rangeEndMillis by remember { mutableStateOf(initialRange?.endInclusive) }
    run {
        val cal = Calendar.getInstance().apply { timeInMillis = initialRange?.startInclusive ?: System.currentTimeMillis() }
        initialYear = cal.get(Calendar.YEAR)
        initialMonth = cal.get(Calendar.MONTH)
    }

    Dialog(onDismissRequest = onDismiss) {
        GlassWindowBlur()
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = glassDialogContainerColor(GlassStyle.Thick),
        ) {
            Column(modifier = Modifier.width(CalendarWidth).padding(16.dp)) {
                Text(
                    text = "Select a date range",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp),
                )
                MonthPagerCalendar(
                    initialYear = initialYear,
                    initialMonth = initialMonth,
                    dayCell = { year, month, day, onClick ->
                        val dayMillis = localDayMillis(year, month, day)
                        val start = rangeStartMillis
                        val end = rangeEndMillis
                        val isStart = start != null && dayMillis == start
                        val isEnd = end != null && dayMillis == end
                        val inRange = start != null && end != null && dayMillis in start..end
                        Box(
                            modifier = Modifier.fillMaxSize().background(
                                if (inRange && !isStart && !isEnd) MaterialTheme.colorScheme.primary.copy(alpha = 0.18f) else Color.Transparent,
                            ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(DayCellSize)
                                    .clip(CircleShape)
                                    .background(if (isStart || isEnd) MaterialTheme.colorScheme.primary else Color.Transparent)
                                    .clickable(onClick = onClick),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = day.toString(),
                                    color = if (isStart || isEnd) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                )
                            }
                        }
                    },
                    onDayClick = { year, month, day ->
                        val dayMillis = localDayMillis(year, month, day)
                        val start = rangeStartMillis
                        val end = rangeEndMillis
                        if (start == null || end != null) {
                            rangeStartMillis = dayMillis
                            rangeEndMillis = null
                        } else if (dayMillis < start) {
                            rangeStartMillis = dayMillis
                        } else {
                            rangeEndMillis = dayMillis
                        }
                    },
                )
                Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    TextButton(
                        onClick = {
                            val start = rangeStartMillis
                            val end = rangeEndMillis
                            if (start != null && end != null) {
                                onConfirm(DateRange(startOfDay(start), endOfDay(end)))
                            }
                            onDismiss()
                        },
                        enabled = rangeStartMillis != null && rangeEndMillis != null,
                    ) { Text("OK") }
                }
            }
        }
    }
}

private fun startOfDay(millis: Long): Long =
    Calendar.getInstance().apply { timeInMillis = millis; set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }.timeInMillis

private fun endOfDay(millis: Long): Long =
    Calendar.getInstance().apply { timeInMillis = millis; set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999) }.timeInMillis
