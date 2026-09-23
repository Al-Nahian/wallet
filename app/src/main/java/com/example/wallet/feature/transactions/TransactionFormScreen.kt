package com.example.wallet.feature.transactions

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.wallet.core.common.minorUnitsToEditableString
import com.example.wallet.core.common.parseMoneyToMinorUnits
import com.example.wallet.core.design.WalletTheme
import com.example.wallet.core.design.components.AmountInput
import com.example.wallet.core.design.components.AmountKeypad
import com.example.wallet.core.design.components.CategoryPickerDialog
import com.example.wallet.core.design.components.CategoryPickerField
import com.example.wallet.core.design.components.GlassDropdownMenu
import com.example.wallet.core.design.components.GlassIconBubble
import com.example.wallet.core.design.components.GlassScreenTopBar
import com.example.wallet.core.design.components.LabelChip
import com.example.wallet.core.design.components.PrimaryButton
import com.example.wallet.core.design.components.SecondaryButton
import com.example.wallet.core.design.components.SelectorOption
import com.example.wallet.core.design.glass.GlassShapes
import com.example.wallet.core.design.glass.GlassStyle
import com.example.wallet.core.design.glass.GlassSurface
import com.example.wallet.core.design.glass.GlassTokens
import com.example.wallet.core.design.glass.GlassWindowBlur
import com.example.wallet.core.design.glass.applyDialogBlurBehind
import com.example.wallet.core.design.glass.glassDialogContainerColor
import com.example.wallet.domain.model.Category
import com.example.wallet.domain.model.CategoryGroup
import com.example.wallet.domain.model.Label
import com.example.wallet.domain.model.TransactionType
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionFormScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TransactionFormViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showDetailsPage by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.saved) {
        if (uiState.saved) onSaved()
    }
    LaunchedEffect(uiState.deleted) {
        if (uiState.deleted) onBack()
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            GlassScreenTopBar(
                title = if (uiState.isEditMode) "Edit Transaction" else "Add Transaction",
                navigationIcon = {
                    IconButton(onClick = { if (showDetailsPage) showDetailsPage = false else onBack() }) {
                        Icon(
                            imageVector = if (showDetailsPage) Icons.AutoMirrored.Filled.ArrowBack else Icons.Filled.Close,
                            contentDescription = if (showDetailsPage) "Back" else "Close",
                        )
                    }
                },
                actions = {
                    if (uiState.isEditMode) {
                        IconButton(onClick = { showDeleteConfirm = true }, enabled = !uiState.isDeleting) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete transaction")
                        }
                    }
                    IconButton(onClick = viewModel::save, enabled = !uiState.isSaving) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Filled.Check, contentDescription = "Save")
                        }
                    }
                },
            )
        },
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        if (showDetailsPage) {
            DetailsPage(uiState = uiState, viewModel = viewModel, paddingValues = paddingValues)
        } else {
            MainPage(uiState = uiState, viewModel = viewModel, paddingValues = paddingValues, onOpenDetails = { showDetailsPage = true })
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete transaction?") },
            text = {
                GlassWindowBlur()
                Text("This removes it from your ledger. You can't undo this from here.")
            },
            confirmButton = {
                TextButton(onClick = { showDeleteConfirm = false; viewModel.delete() }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            },
        )
    }
}

/** Page 1 — type, amount, and the account/category (or from/to account) pair, exactly the fields
 * needed to post a minimal transaction; everything else lives on [DetailsPage]. */
@Composable
private fun MainPage(
    uiState: TransactionFormState,
    viewModel: TransactionFormViewModel,
    paddingValues: androidx.compose.foundation.layout.PaddingValues,
    onOpenDetails: () -> Unit,
) {
    val income = WalletTheme.extendedColors.income
    val expense = WalletTheme.extendedColors.expense
    val transfer = WalletTheme.extendedColors.transfer
    val accent = WalletTheme.extendedColors.accent
    val amountTint = when (uiState.type) {
        TransactionType.INCOME -> income
        TransactionType.EXPENSE -> expense
        else -> transfer
    }

    Column(
        modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            TransactionTypeSegmented(
                selected = uiState.type,
                isEditMode = uiState.isEditMode,
                onSelected = viewModel::onTypeChange,
                incomeColor = income,
                expenseColor = expense,
                transferColor = transfer,
            )

            AmountGlassCard(
                amountInput = uiState.amountInput,
                tint = amountTint,
                onOpenDetails = onOpenDetails,
            )

            if (uiState.type == TransactionType.TRANSFER) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    GlassAccountPickerCard(
                        label = "From Account",
                        icon = Icons.Filled.AccountBalance,
                        tint = transfer,
                        options = uiState.accountOptions,
                        selectedId = uiState.accountId,
                        onSelected = viewModel::onAccountChange,
                        modifier = Modifier.weight(1f),
                    )
                    GlassAccountPickerCard(
                        label = "To Account",
                        icon = Icons.Filled.AccountBalance,
                        tint = accent,
                        options = uiState.accountOptions.filter { it.id != uiState.accountId },
                        selectedId = uiState.toAccountId,
                        onSelected = viewModel::onToAccountChange,
                        modifier = Modifier.weight(1f),
                    )
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    GlassAccountPickerCard(
                        label = "Account",
                        icon = Icons.Filled.AccountBalance,
                        tint = transfer,
                        options = uiState.accountOptions,
                        selectedId = uiState.accountId,
                        onSelected = viewModel::onAccountChange,
                        modifier = Modifier.weight(1f),
                    )
                    if (!uiState.isSplitEnabled) {
                        GlassCategoryPickerCard(
                            tint = accent,
                            groups = uiState.categoryGroups,
                            categories = uiState.categories,
                            selectedCategoryId = uiState.categoryId,
                            onSelected = viewModel::onCategoryChange,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }

                SecondaryButton(
                    text = if (uiState.isSplitEnabled) "Remove split" else "Split this transaction",
                    onClick = { viewModel.onToggleSplit(!uiState.isSplitEnabled) },
                    modifier = Modifier.fillMaxWidth(),
                )

                if (uiState.isSplitEnabled) {
                    SplitEditor(
                        rows = uiState.splitRows,
                        categoryGroups = uiState.categoryGroups,
                        categories = uiState.categories,
                        targetAmountInput = uiState.amountInput,
                        onAddRow = viewModel::addSplitRow,
                        onRemoveRow = viewModel::removeSplitRow,
                        onCategoryChange = viewModel::onSplitCategoryChange,
                        onAmountChange = viewModel::onSplitAmountChange,
                        onNoteChange = viewModel::onSplitNoteChange,
                    )
                }
            }

            if (uiState.errorMessage != null) {
                Text(
                    text = uiState.errorMessage.orEmpty(),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        AmountKeypad(
            onKeyPress = viewModel::onAmountKeypadKey,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).imePadding(),
        )
    }
}

/** Page 2 — everything that isn't needed to post a minimal transaction: note, labels, payee,
 * date, time, and place. Reached via the amount card's trailing arrow. */
@Composable
private fun DetailsPage(
    uiState: TransactionFormState,
    viewModel: TransactionFormViewModel,
    paddingValues: androidx.compose.foundation.layout.PaddingValues,
) {
    val rowTint = MaterialTheme.colorScheme.surfaceVariant

    Column(
        modifier = Modifier
            .padding(paddingValues)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
            .imePadding()
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        GlassTextRow(
            icon = Icons.AutoMirrored.Filled.Notes,
            tint = rowTint,
            label = "Note",
            value = uiState.note,
            placeholder = "Add a note (optional)",
            onValueChange = viewModel::onNoteChange,
        )

        GlassLabelsRow(
            tint = rowTint,
            allLabels = uiState.labelOptions,
            selectedLabelIds = uiState.selectedLabelIds,
            onToggleLabel = viewModel::onToggleLabel,
        )

        if (uiState.type != TransactionType.TRANSFER) {
            GlassTextRow(
                icon = Icons.Filled.Person,
                tint = rowTint,
                label = "Payee",
                value = uiState.payee,
                placeholder = "Enter payee name",
                onValueChange = viewModel::onPayeeChange,
            )
        }

        GlassDateRow(tint = rowTint, dateMillis = uiState.date, onDateChange = viewModel::onDateChange)
        GlassTimeRow(tint = rowTint, dateMillis = uiState.date, onTimeChange = viewModel::onDateChange)

        GlassTextRow(
            icon = Icons.Filled.Place,
            tint = rowTint,
            label = "Place",
            value = uiState.place,
            placeholder = "Add place (optional)",
            onValueChange = viewModel::onPlaceChange,
        )
    }
}

/** plan.md §10/§22 — Expense/Income/Transfer as one glowing pill toggle. Transfer can't be
 * switched to (or from) in edit mode: transfers have no edit support yet, and an existing
 * expense/income can't be converted into a transfer's two linked rows via this same edit flow. */
@Composable
private fun TransactionTypeSegmented(
    selected: TransactionType,
    isEditMode: Boolean,
    onSelected: (TransactionType) -> Unit,
    incomeColor: Color,
    expenseColor: Color,
    transferColor: Color,
) {
    data class Segment(val type: TransactionType, val icon: androidx.compose.ui.graphics.vector.ImageVector, val label: String, val color: Color)

    val segments = buildList {
        add(Segment(TransactionType.INCOME, Icons.Filled.ArrowUpward, "Income", incomeColor))
        add(Segment(TransactionType.EXPENSE, Icons.Filled.ArrowDownward, "Expense", expenseColor))
        if (!isEditMode) add(Segment(TransactionType.TRANSFER, Icons.Filled.SwapHoriz, "Transfer", transferColor))
    }

    GlassSurface(
        modifier = Modifier.fillMaxWidth(),
        style = GlassStyle.Thin,
        shape = GlassShapes.pill,
        elevation = 0.dp,
    ) {
        Row(modifier = Modifier.padding(4.dp).fillMaxWidth()) {
            segments.forEach { segment ->
                val isSelected = segment.type == selected
                Box(modifier = Modifier.weight(1f)) {
                    if (isSelected) {
                        GlassSurface(
                            modifier = Modifier.fillMaxWidth().clickable { onSelected(segment.type) },
                            style = GlassStyle.Vivid,
                            tint = segment.color,
                            fill = segment.color.copy(alpha = 0.9f),
                            glow = true,
                            shape = GlassShapes.pill,
                            elevation = 0.dp,
                        ) {
                            SegmentLabel(segment.icon, segment.label, Color.White)
                        }
                    } else {
                        Box(
                            modifier = Modifier.fillMaxWidth().clickable { onSelected(segment.type) },
                        ) {
                            SegmentLabel(segment.icon, segment.label, MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SegmentLabel(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(6.dp))
        Text(text = label, color = color, fontWeight = FontWeight.Medium, fontSize = 14.sp)
    }
}

private val transparentTextFieldColors
    @Composable get() = TextFieldDefaults.colors(
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent,
        disabledContainerColor = Color.Transparent,
        focusedIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent,
        disabledIndicatorColor = Color.Transparent,
        cursorColor = MaterialTheme.colorScheme.onSurface,
    )

/** The amount card: shows the amount [AmountKeypad] is building below it (never the system IME —
 * see plans notes on why), plus a trailing circular button that opens [DetailsPage] — matches the
 * reference design's two-step flow. */
@Composable
private fun AmountGlassCard(
    amountInput: String,
    tint: Color,
    onOpenDetails: () -> Unit,
) {
    GlassSurface(
        modifier = Modifier.fillMaxWidth(),
        style = GlassStyle.Vivid,
        tint = tint,
        fill = tint.copy(alpha = GlassTokens.frostedFillAlpha),
        glow = true,
        shape = GlassShapes.large,
        elevation = 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            GlassIconBubble(icon = Icons.Filled.Payments, tint = tint, size = 40.dp)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Amount", fontSize = 12.sp, color = Color.White.copy(alpha = 0.85f))
                Text(
                    text = amountInput.ifEmpty { "0.00" },
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (amountInput.isEmpty()) Color.White.copy(alpha = 0.5f) else Color.White,
                )
            }
            IconButton(
                onClick = onOpenDetails,
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.White.copy(alpha = 0.25f), CircleShape),
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = "More details", tint = Color.White, modifier = Modifier.size(16.dp))
            }
        }
    }
}

/** A flat dropdown of accounts, styled as a glass card instead of [SelectorField]'s outlined
 * text field — used for Account/From Account/To Account, which sit side by side in a grid. */
@Composable
private fun GlassAccountPickerCard(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    options: List<SelectorOption>,
    selectedId: String?,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = options.firstOrNull { it.id == selectedId }?.label ?: "Select"

    GlassSurface(
        modifier = modifier,
        style = GlassStyle.Vivid,
        tint = tint,
        fill = tint.copy(alpha = GlassTokens.frostedFillAlpha),
        elevation = 0.dp,
    ) {
        Box {
            Row(
                modifier = Modifier.fillMaxWidth().clickable { expanded = true }.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                GlassIconBubble(icon = icon, tint = tint, size = 32.dp)
                Spacer(Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = label, fontSize = 11.sp, color = Color.White.copy(alpha = 0.85f))
                    Text(
                        text = selectedLabel,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        maxLines = 1,
                    )
                }
            }
            GlassDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, tint = tint) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.label) },
                        onClick = {
                            onSelected(option.id)
                            expanded = false
                        },
                    )
                }
            }
        }
    }
}

/** A [CategoryPickerDialog]-backed glass card — used for Category (and would back "To Category"
 * if the domain ever needed one; transfers use [GlassAccountPickerCard] for "To Account"
 * instead, since a transfer has no category). */
@Composable
private fun GlassCategoryPickerCard(
    tint: Color,
    groups: List<CategoryGroup>,
    categories: List<Category>,
    selectedCategoryId: String?,
    onSelected: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showPicker by remember { mutableStateOf(false) }
    val selectedLabel = categories.firstOrNull { it.id == selectedCategoryId }?.name ?: "Uncategorized"

    GlassSurface(
        modifier = modifier,
        style = GlassStyle.Vivid,
        tint = tint,
        fill = tint.copy(alpha = GlassTokens.frostedFillAlpha),
        elevation = 0.dp,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().clickable { showPicker = true }.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            GlassIconBubble(icon = Icons.Filled.Category, tint = tint, size = 32.dp)
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Category", fontSize = 11.sp, color = Color.White.copy(alpha = 0.85f))
                Text(
                    text = selectedLabel,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    maxLines = 1,
                )
            }
        }
    }

    if (showPicker) {
        CategoryPickerDialog(
            groups = groups,
            categories = categories,
            onSelected = { id ->
                onSelected(id)
                showPicker = false
            },
            onDismiss = { showPicker = false },
        )
    }
}

/** A page-2 row: leading icon bubble, small label, and an inline-editable value — Note/Payee/
 * Place all share this shape. */
@Composable
private fun GlassTextRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    label: String,
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
) {
    GlassSurface(
        modifier = Modifier.fillMaxWidth(),
        style = GlassStyle.Thin,
        shape = GlassShapes.medium,
        elevation = 0.dp,
    ) {
        Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            GlassIconBubble(icon = icon, tint = tint, size = 32.dp)
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                TextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    textStyle = TextStyle(fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface),
                    placeholder = { Text(placeholder, fontSize = 15.sp) },
                    colors = transparentTextFieldColors,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun GlassDateRow(tint: Color, dateMillis: Long, onDateChange: (Long) -> Unit) {
    val context = LocalContext.current
    val formatted = remember(dateMillis) {
        SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(dateMillis))
    }
    GlassSurface(
        modifier = Modifier.fillMaxWidth(),
        style = GlassStyle.Thin,
        shape = GlassShapes.medium,
        elevation = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    val calendar = Calendar.getInstance().apply { timeInMillis = dateMillis }
                    DatePickerDialog(
                        context,
                        { _, year, month, dayOfMonth ->
                            val picked = Calendar.getInstance().apply {
                                timeInMillis = dateMillis
                                set(year, month, dayOfMonth)
                            }
                            onDateChange(picked.timeInMillis)
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH),
                    ).apply { window?.let { applyDialogBlurBehind(it) } }.show()
                }
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            GlassIconBubble(icon = Icons.Filled.CalendarMonth, tint = tint, size = 32.dp)
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Date", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = formatted, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

@Composable
private fun GlassTimeRow(tint: Color, dateMillis: Long, onTimeChange: (Long) -> Unit) {
    val context = LocalContext.current
    val formatted = remember(dateMillis) {
        SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(dateMillis))
    }
    GlassSurface(
        modifier = Modifier.fillMaxWidth(),
        style = GlassStyle.Thin,
        shape = GlassShapes.medium,
        elevation = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
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
                }
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            GlassIconBubble(icon = Icons.Filled.AccessTime, tint = tint, size = 32.dp)
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Time", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = formatted, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

/** plan.md §13 split entry: category + amount rows plus a running total vs. the transaction's
 * own amount, so a mismatched split is visible before Save is even tapped. */
@Composable
private fun SplitEditor(
    rows: List<SplitRowState>,
    categoryGroups: List<CategoryGroup>,
    categories: List<Category>,
    targetAmountInput: String,
    onAddRow: () -> Unit,
    onRemoveRow: (String) -> Unit,
    onCategoryChange: (String, String) -> Unit,
    onAmountChange: (String, String) -> Unit,
    onNoteChange: (String, String) -> Unit,
) {
    val targetMinor = parseMoneyToMinorUnits(targetAmountInput) ?: 0L
    val runningTotal = rows.sumOf { parseMoneyToMinorUnits(it.amountInput) ?: 0L }
    val isBalanced = runningTotal == targetMinor

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        rows.forEach { row ->
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    CategoryPickerField(
                        groups = categoryGroups,
                        categories = categories,
                        selectedCategoryId = row.categoryId,
                        onSelected = { id -> id?.let { onCategoryChange(row.key, it) } },
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = { onRemoveRow(row.key) }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Remove split")
                    }
                }
                AmountInput(
                    value = row.amountInput,
                    onValueChange = { onAmountChange(row.key, it) },
                    label = "Split amount",
                )
                TextField(
                    value = row.note,
                    onValueChange = { onNoteChange(row.key, it) },
                    label = { Text("Note (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        SecondaryButton(text = "Add split", onClick = onAddRow, modifier = Modifier.fillMaxWidth())

        Text(
            text = "Split total: ${minorUnitsToEditableString(runningTotal)} of ${minorUnitsToEditableString(targetMinor)}",
            style = MaterialTheme.typography.bodyMedium,
            color = if (isBalanced) WalletTheme.extendedColors.income else MaterialTheme.colorScheme.error,
        )
    }
}

/** plan.md §16: multi-select label picker, styled as a page-2 glass row — selected labels show
 * as removable chips, with a dialog listing every label as a checkbox row to add/remove. */
@Composable
private fun GlassLabelsRow(
    tint: Color,
    allLabels: List<Label>,
    selectedLabelIds: Set<String>,
    onToggleLabel: (String) -> Unit,
) {
    var showPicker by remember { mutableStateOf(false) }
    val selectedLabels = allLabels.filter { it.id in selectedLabelIds }

    GlassSurface(
        modifier = Modifier.fillMaxWidth(),
        style = GlassStyle.Thin,
        shape = GlassShapes.medium,
        elevation = 0.dp,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().clickable { showPicker = true }.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            GlassIconBubble(icon = Icons.Filled.Sell, tint = tint, size = 32.dp)
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Labels", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (selectedLabels.isEmpty()) {
                    Text(text = "Add label (optional)", fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.horizontalScroll(rememberScrollState()).padding(top = 2.dp),
                    ) {
                        selectedLabels.forEach { label -> LabelChip(name = label.name, color = label.color) }
                    }
                }
            }
        }
    }

    if (showPicker) {
        AlertDialog(
            onDismissRequest = { showPicker = false },
            title = { Text("Labels") },
            text = {
                GlassWindowBlur()
                if (allLabels.isEmpty()) {
                    Text("No labels yet. Create some from Profile > Manage labels.")
                } else {
                    Column {
                        allLabels.forEach { label ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .clickable { onToggleLabel(label.id) },
                            ) {
                                Checkbox(checked = label.id in selectedLabelIds, onCheckedChange = { onToggleLabel(label.id) })
                                Spacer(Modifier.width(8.dp))
                                LabelChip(name = label.name, color = label.color)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPicker = false }) { Text("Done") }
            },
            containerColor = glassDialogContainerColor(GlassStyle.Thick),
        )
    }
}
