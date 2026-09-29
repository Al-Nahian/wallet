package com.example.wallet.feature.transactions

import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import kotlinx.coroutines.launch
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.example.wallet.core.design.components.GlassScreenScaffold
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.wallet.core.common.formatMoney
import com.example.wallet.core.design.DarkPrimary
import com.example.wallet.core.design.WalletTheme
import com.example.wallet.core.design.components.AmountKeypad
import com.example.wallet.core.design.components.CategoryPickerDialog
import com.example.wallet.core.design.components.categoryIcon
import com.example.wallet.core.design.components.ConfirmationDialog
import com.example.wallet.core.design.components.GlassIconBubble
import com.example.wallet.core.design.components.formatAmountEntryForDisplay
import com.example.wallet.core.design.components.GlassScreenTopBar
import com.example.wallet.core.design.components.LabelChip
import com.example.wallet.core.design.components.LiquidGlassCard
import com.example.wallet.core.design.components.defaultWaveVariant
import com.example.wallet.core.design.glass.GlassColors
import com.example.wallet.core.design.glass.GlassShapes
import com.example.wallet.core.design.glass.GlassStyle
import com.example.wallet.core.design.glass.GlassSurface
import com.example.wallet.core.design.glass.GlassTokens
import com.example.wallet.core.design.glass.GlassWindowBlur
import com.example.wallet.core.design.glass.glassDialogContainerColor
import com.example.wallet.core.design.components.GlassDatePickerDialog
import com.example.wallet.core.design.components.GlassTimePickerDialog
import com.example.wallet.domain.model.Category
import com.example.wallet.domain.model.CategoryGroup
import com.example.wallet.domain.model.Label
import com.example.wallet.domain.model.Template
import com.example.wallet.domain.model.TransactionType
import com.example.wallet.feature.accounts.icon
import com.example.wallet.feature.labels.LabelFormDialog
import com.example.wallet.feature.templates.TemplateFormDialog
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Shared floor for every details-page row (Note/Labels/Payee/Date/Time/Place) so a plain
 * two-line Text row (Date, Time) doesn't end up visibly shorter than a [TextField]-backed one
 * (Note, Payee, Place) — they used to size to their own content and read as uneven card heights. */
private val DetailRowMinHeight = 72.dp

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

    GlassScreenScaffold(
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
            return@GlassScreenScaffold
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
    // Currency shown on the amount card ("BDT 0.00" in the reference): the selected account's
    // own currency, else the first account's, else the app's BDT seed default.
    val currency = uiState.accountOptions.firstOrNull { it.id == uiState.accountId }?.currency
        ?: uiState.accountOptions.firstOrNull()?.currency ?: "BDT"
    val isDark = isSystemInDarkTheme()

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
            verticalArrangement = Arrangement.spacedBy(8.dp),
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
                equation = uiState.amountEquation,
                currency = currency,
                icon = when (uiState.type) {
                    TransactionType.INCOME -> Icons.Filled.Add
                    TransactionType.EXPENSE -> Icons.Filled.Remove
                    TransactionType.TRANSFER, TransactionType.REFUND, TransactionType.ADJUSTMENT -> null
                },
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

            GlassTemplateRow(
                tint = DarkPrimary,
                templates = uiState.templates,
                appliedTemplateId = uiState.appliedTemplateId,
                accountOptions = uiState.accountOptions,
                categoryOptions = uiState.categories,
                labelOptions = uiState.labelOptions,
                currentAccountId = uiState.accountId,
                currentCategoryId = uiState.categoryId,
                currentLabelIds = uiState.selectedLabelIds,
                currentPayee = uiState.payee,
                currentPlace = uiState.place,
                onApplyTemplate = viewModel::applyTemplate,
                onCreateTemplate = viewModel::createTemplate,
                onUpdateTemplate = viewModel::updateTemplate,
                onDeleteTemplate = viewModel::deleteTemplate,
            )

            if (uiState.errorMessage != null) {
                Text(
                    text = uiState.errorMessage.orEmpty(),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        // Reference light design seats the keys on a faint white panel; dark mode keeps them
        // floating directly on the page (confirmed look, untouched).
        if (isDark) {
            AmountKeypad(
                onKeyPress = viewModel::onAmountKeypadKey,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).imePadding(),
            )
        } else {
            Box(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .imePadding()
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(28.dp),
                        clip = false,
                        ambientColor = Color(0xFF5B7A9C).copy(alpha = 0.12f),
                        spotColor = Color(0xFF5B7A9C).copy(alpha = 0.12f),
                    )
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color.White.copy(alpha = 0.7f))
                    .border(1.dp, Color.White.copy(alpha = 0.8f), RoundedCornerShape(28.dp))
                    .padding(8.dp),
            ) {
                AmountKeypad(
                    onKeyPress = viewModel::onAmountKeypadKey,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

/** Page 2 — everything that isn't needed to post a minimal transaction: note, labels, payee,
 * date, time, and place. Reached via the amount card's trailing arrow. Each row is tinted
 * glass with a matching bubble/label/chevron (Add-Transaction-Reference): blue note, purple
 * labels, teal payee, orange date, blue time, purple place. */
@Composable
private fun DetailsPage(
    uiState: TransactionFormState,
    viewModel: TransactionFormViewModel,
    paddingValues: androidx.compose.foundation.layout.PaddingValues,
) {
    val extended = WalletTheme.extendedColors
    val teal = MaterialTheme.colorScheme.secondary
    val context = LocalContext.current

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearError()
        }
    }

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
            tint = extended.transfer,
            label = "Note",
            value = uiState.note,
            placeholder = "Add a note (optional)",
            onValueChange = viewModel::onNoteChange,
        )

        GlassLabelsRow(
            tint = extended.accent,
            allLabels = uiState.labelOptions,
            selectedLabelIds = uiState.selectedLabelIds,
            onToggleLabel = viewModel::onToggleLabel,
            onCreateLabel = viewModel::createLabel,
        )

        if (uiState.type != TransactionType.TRANSFER) {
            GlassTextRow(
                icon = Icons.Filled.Person,
                tint = teal,
                label = "Payee",
                value = uiState.payee,
                placeholder = "Enter payee name",
                onValueChange = viewModel::onPayeeChange,
                suggestions = uiState.payeeSuggestions,
            )
        }

        GlassDateRow(tint = extended.warning, dateMillis = uiState.date, onDateChange = viewModel::onDateChange)
        GlassTimeRow(tint = extended.transfer, dateMillis = uiState.date, onTimeChange = viewModel::onDateChange)

        GlassTextRow(
            icon = Icons.Filled.Place,
            tint = extended.accent,
            label = "Place",
            value = uiState.place,
            placeholder = "Add place (optional)",
            onValueChange = viewModel::onPlaceChange,
            suggestions = uiState.placeSuggestions,
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

    LiquidGlassCard(
        modifier = Modifier.fillMaxWidth(),
        tint = GlassColors.neutralGlassTint(WalletTheme.extendedColors.transfer),
        shape = GlassShapes.pill,
        cornerRadius = 999.dp,
        lightweight = true,
    ) {
        BoxWithConstraints(modifier = Modifier.padding(4.dp).fillMaxWidth()) {
            val segmentWidth = maxWidth / segments.size
            val selectedIndex = segments.indexOfFirst { it.type == selected }.coerceAtLeast(0)
            val targetLeft = segmentWidth * selectedIndex
            val targetRight = segmentWidth * (selectedIndex + 1)

            // A liquid-droplet stretch rather than a rigid slide: the two edges of the pill are
            // independent springs, not one shared offset+width tween. Whichever edge leads in the
            // direction of travel gets the stiffer spring and arrives first, so the pill stretches
            // out ahead of itself before the trailing edge catches up and the shape snaps back to
            // its resting width — the same "reach, then catch up" motion a drop of liquid makes.
            val leftEdge = remember { Animatable(targetLeft, Dp.VectorConverter) }
            val rightEdge = remember { Animatable(targetRight, Dp.VectorConverter) }
            LaunchedEffect(targetLeft, targetRight) {
                val movingRight = targetLeft > leftEdge.value
                val leadSpring = spring<Dp>(dampingRatio = 0.68f, stiffness = 420f)
                val followSpring = spring<Dp>(dampingRatio = 0.68f, stiffness = 190f)
                if (movingRight) {
                    launch { rightEdge.animateTo(targetRight, leadSpring) }
                    launch { leftEdge.animateTo(targetLeft, followSpring) }
                } else {
                    launch { leftEdge.animateTo(targetLeft, leadSpring) }
                    launch { rightEdge.animateTo(targetRight, followSpring) }
                }
            }

            val indicatorColor by animateColorAsState(
                targetValue = segments[selectedIndex].color,
                animationSpec = tween(durationMillis = 280),
                label = "segmentIndicatorColor",
            )

            // height(IntrinsicSize.Min) lets the sliding indicator's fillMaxHeight() resolve
            // against the label Row's real (wrap-content) height instead of an unbounded parent
            // max height, which would otherwise make it collapse to zero — the same pitfall
            // AmountGlassCard's digits hit earlier.
            Box(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                val left = leftEdge.value.coerceAtMost(rightEdge.value)
                val right = rightEdge.value.coerceAtLeast(leftEdge.value)
                // Glossy liquid-glass pill (reference design's glowing red Expense tab) rather
                // than a flat fill — the reflection/border layers carry the sheen.
                LiquidGlassCard(
                    tint = indicatorColor,
                    modifier = Modifier
                        .offset(x = left)
                        .width(right - left)
                        .fillMaxHeight(),
                    shape = GlassShapes.pill,
                    cornerRadius = 999.dp,
                    lightweight = true,
                ) {}

                Row(modifier = Modifier.fillMaxWidth()) {
                    segments.forEach { segment ->
                        val isSelected = segment.type == selected
                        val labelColor by animateColorAsState(
                            targetValue = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            animationSpec = tween(durationMillis = 280),
                            label = "segmentLabelColor",
                        )
                        Box(
                            modifier = Modifier.weight(1f).clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                            ) { onSelected(segment.type) },
                        ) {
                            SegmentLabel(segment.icon, segment.label, labelColor)
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
 * reference design's two-step flow. A third, always-reserved line above the amount shows the
 * equation just evaluated by "=" (e.g. "120+35"), greyed out, calculator-style — reserved even
 * when empty so the card's height (and the amount's vertical position) doesn't jump between
 * plain-number and post-equation states.
 *
 * Amounts render through [formatAmountEntryForDisplay] (grouping + 2 decimals, live), with the
 * currency code tucked at the card's far bottom-right and the digits where the code used to be.
 * A slim caret blinks after the digits like a calculator display (static when reduced motion is
 * on). Light mode adds an "Amount" caption over dark-navy digits; dark mode keeps its confirmed
 * white treatment.
 *
 * Rendered with [LiquidGlassCard]'s full multi-layer dark-mode glass (base gradient, glow, waves,
 * specular streak, inner border) so it reads as the reference's glossy red hero card rather than
 * a flat tinted panel. */
@Composable
private fun AmountGlassCard(
    amountInput: String,
    equation: String?,
    currency: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector?,
    tint: Color,
    onOpenDetails: () -> Unit,
) {
    val isDark = isSystemInDarkTheme()
    // Dimmed while empty so the "0.00" placeholder sits back like the reference's; full strength
    // once the user has typed anything.
    val digitsColor = if (isDark) {
        if (amountInput.isEmpty()) Color.White.copy(alpha = 0.5f) else Color.White
    } else {
        val base = MaterialTheme.colorScheme.onSurface
        if (amountInput.isEmpty()) base.copy(alpha = 0.45f) else base
    }
    val codeColor = if (isDark) {
        Color.White.copy(alpha = 0.55f)
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    LiquidGlassCard(
        tint = tint,
        modifier = Modifier.fillMaxWidth(),
        shape = GlassShapes.large,
        cornerRadius = GlassTokens.cornerLarge,
        // One shared wave for every transaction type — Income's — so switching tabs only
        // recolors the card instead of reshaping its gloss (each tint previously hashed to
        // its own wave variant, which is why Income/Expense/Transfer all looked different).
        waveVariant = defaultWaveVariant(WalletTheme.extendedColors.income),
    ) {
        // A tall "hero" display, reaching down to about where the Account/Category row used to
        // end, so the card reads as a calculator-style amount display rather than a thin banner —
        // Account/Category and the split button sit below it in the Column, so they simply move
        // down to make room. The icon sits at the top, the digits at the bottom (like a
        // calculator screen); the chevron button is centered on the card's full height. heightIn
        // (min) has to be on this Column itself, not on the wrapping Box: a plain Box measures
        // non-matchParentSize content with minHeight loosened to 0, so a Column below it doing
        // Modifier.fillMaxSize() would fall back to wrap-content height, leaving the weighted
        // Spacer nothing to expand into and the digits stuck near the top instead of the bottom.
        Box(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
        ) {
            Column(modifier = Modifier.fillMaxWidth().heightIn(min = 140.dp).padding(end = 52.dp)) {
                // No icon for a transfer — it isn't a "+" or "-" against the account it's shown
                // on, so neither arithmetic sign would be accurate.
                if (icon != null) {
                    GlassIconBubble(icon = icon, tint = tint, size = 40.dp)
                }
                Spacer(Modifier.weight(1f))
                Text(
                    text = equation?.let { formatAmountEntryForDisplay(it) }.orEmpty(),
                    fontSize = 14.sp,
                    color = if (isDark) {
                        Color.White.copy(alpha = 0.55f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    },
                    maxLines = 1,
                )
                if (!isDark) {
                    Text(
                        text = "Amount",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Text(
                        text = formatAmountEntryForDisplay(amountInput),
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Bold,
                        color = digitsColor,
                        maxLines = 1,
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = currency,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = codeColor,
                        maxLines = 1,
                    )
                }
            }
            IconButton(
                onClick = onOpenDetails,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(40.dp)
                    .background(Color.White.copy(alpha = 0.25f), CircleShape),
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = "More details", tint = Color.White, modifier = Modifier.size(16.dp))
            }
        }
    }
}

/** Darkens [this] toward black by [amount] (0f = unchanged, 1f = black) — a card's own value text
 * in light mode needs to read as a deeper shade of the tile's pastel tint, not the tint itself
 * (too close in value to the pastel glass surface underneath it) and not a flat neutral black
 * (loses the per-card color identity Account/Template share with Category's own accent-colored
 * value text). */
private fun Color.darkenForLightCard(amount: Float = 0.45f): Color = lerp(this, Color.Black, amount)

/** A glossy account picker card (reference design's blue "Account / Select" tile) — tapping it
 * opens a centered [AccountPickerDialog] popup listing every account with its live balance
 * (select-account-reference.png) instead of an anchored dropdown. Used for Account/From
 * Account/To Account, which sit side by side in a grid. */
@Composable
private fun GlassAccountPickerCard(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    options: List<AccountPickerOption>,
    selectedId: String?,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showPicker by remember { mutableStateOf(false) }
    val selectedLabel = options.firstOrNull { it.id == selectedId }?.name ?: "Select account"
    // Reference light design: dark-navy value text on the pastel tile (not white), plus a
    // trailing dropdown chevron. Dark mode keeps its confirmed white treatment untouched.
    val isDark = isSystemInDarkTheme()

    LiquidGlassCard(
        tint = tint,
        modifier = modifier,
        lightweight = true,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().clickable { showPicker = true }.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            GlassIconBubble(icon = icon, tint = tint, size = 32.dp)
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    fontSize = 11.sp,
                    color = if (isDark) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = selectedLabel,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDark) Color.White else tint.darkenForLightCard(0.3f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (!isDark) {
                Icon(
                    Icons.Filled.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }

    if (showPicker) {
        AccountPickerDialog(
            title = "Select $label",
            options = options,
            selectedId = selectedId,
            onSelected = { id ->
                onSelected(id)
                showPicker = false
            },
            onDismiss = { showPicker = false },
        )
    }
}

/** Centered glass popup listing every account with its live balance (select-account-reference),
 * with the chosen row highlighted as a blue pill carrying a check bubble. The page behind is
 * genuinely blurred ([GlassWindowBlur]); the panel itself is blue liquid glass. */
@Composable
private fun AccountPickerDialog(
    title: String,
    options: List<AccountPickerOption>,
    selectedId: String?,
    onSelected: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val extended = WalletTheme.extendedColors
    // Cycles blue → purple → green like the reference's per-account bubbles.
    val bubbleTints = listOf(extended.transfer, extended.accent, extended.income)
    val isDark = isSystemInDarkTheme()

    Dialog(onDismissRequest = onDismiss) {
        // Blue-tinted liquid glass (select-account-reference) rather than the neutral GlassSheet:
        // the reference popup is a blue glass panel, and LiquidGlassCard's gradient + border
        // carries that. GlassWindowBlur still blurs the page behind the dialog itself.
        GlassWindowBlur()
        LiquidGlassCard(
            tint = WalletTheme.extendedColors.transfer,
            modifier = Modifier.fillMaxWidth().heightIn(max = 560.dp),
            shape = GlassShapes.large,
            cornerRadius = GlassTokens.cornerLarge,
        ) {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
                ) {
                    GlassIconBubble(icon = Icons.Filled.AccountBalanceWallet, tint = extended.transfer, size = 32.dp)
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                if (options.isEmpty()) {
                    Text(
                        text = "No accounts yet — add one from the Accounts tab first.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                    )
                } else {
                    LazyColumn {
                        itemsIndexed(options, key = { _, option -> option.id }) { index, option ->
                            val selected = option.id == selectedId
                            val row = @Composable {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                                ) {
                                    GlassIconBubble(
                                        icon = option.type.icon(),
                                        tint = bubbleTints[index % bubbleTints.size],
                                        size = 40.dp,
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = option.name,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                        )
                                        Text(
                                            text = formatMoney(option.balanceMinor, option.currency),
                                            fontSize = 13.sp,
                                            color = if (isDark) Color.White.copy(alpha = 0.75f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                        )
                                    }
                                    if (selected) {
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(if (isDark) Color.White.copy(alpha = 0.25f) else Color.Black.copy(alpha = 0.12f)),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Icon(
                                                Icons.Filled.Check,
                                                contentDescription = null,
                                                tint = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.size(16.dp),
                                            )
                                        }
                                    }
                                }
                            }
                            if (selected) {
                                LiquidGlassCard(
                                    tint = extended.transfer,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                        .clickable { onSelected(option.id) },
                                    shape = GlassShapes.pill,
                                    cornerRadius = 999.dp,
                                    lightweight = true,
                                ) {
                                    // LiquidGlassCard's content scope is a Box — re-anchor the row.
                                    Box(modifier = Modifier.fillMaxWidth()) { row() }
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                        .clickable { onSelected(option.id) },
                                ) {
                                    row()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/** A [CategoryPickerDialog]-backed glossy card (reference design's purple
 * "Category / Uncategorized" tile) — used for Category (transfers use [GlassAccountPickerCard]
 * for "To Account" instead, since a transfer has no category). */
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
    val selectedCategory = categories.firstOrNull { it.id == selectedCategoryId }
    val selectedLabel = selectedCategory?.name ?: "Uncategorized"
    // Reference light design: tag glyph (not the geometric mark), purple value text and a
    // trailing chevron on the pastel tile. Dark mode keeps its confirmed treatment untouched.
    val isDark = isSystemInDarkTheme()

    LiquidGlassCard(
        tint = tint,
        modifier = modifier,
        lightweight = true,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().clickable { showPicker = true }.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            GlassIconBubble(
                icon = selectedCategory?.let { categoryIcon(it.name) }
                    ?: if (isDark) Icons.Filled.Category else Icons.Filled.Sell,
                tint = tint,
                size = 32.dp,
            )
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Category",
                    fontSize = 11.sp,
                    color = if (isDark) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = selectedLabel,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDark) Color.White else WalletTheme.extendedColors.accent,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (!isDark) {
                Icon(
                    Icons.Filled.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }

    if (showPicker) {
        CategoryPickerDialog(
            groups = groups,
            categories = categories,
            selectedCategoryId = selectedCategoryId,
            onSelected = { id ->
                onSelected(id)
                showPicker = false
            },
            onDismiss = { showPicker = false },
        )
    }
}

/** A saved fixed-account/category/label/payee/place shortcut, applied in one tap — new templates
 * can be added, and existing ones edited or deleted, all from this same card's picker dialog
 * without leaving the form (plan requirement: manageable from both Profile > Manage templates and
 * here). */
@Composable
private fun GlassTemplateRow(
    tint: Color,
    templates: List<Template>,
    appliedTemplateId: String?,
    accountOptions: List<AccountPickerOption>,
    categoryOptions: List<Category>,
    labelOptions: List<Label>,
    currentAccountId: String?,
    currentCategoryId: String?,
    currentLabelIds: Set<String>,
    currentPayee: String,
    currentPlace: String,
    onApplyTemplate: (String) -> Unit,
    onCreateTemplate: (name: String, accountId: String?, categoryId: String?, labelId: String?, payee: String?, place: String?) -> Unit,
    onUpdateTemplate: (
        id: String,
        name: String,
        accountId: String?,
        categoryId: String?,
        labelId: String?,
        payee: String?,
        place: String?,
    ) -> Unit,
    onDeleteTemplate: (String) -> Unit,
) {
    var showPicker by remember { mutableStateOf(false) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var editingTemplate by remember { mutableStateOf<Template?>(null) }
    var pendingDeleteTemplate by remember { mutableStateOf<Template?>(null) }
    val accountPairs = remember(accountOptions) { accountOptions.map { it.id to it.name } }
    val categoryPairs = remember(categoryOptions) { categoryOptions.map { it.id to it.name } }
    val labelPairs = remember(labelOptions) { labelOptions.map { it.id to it.name } }
    val accountNamesById = remember(accountPairs) { accountPairs.toMap() }
    val categoryNamesById = remember(categoryPairs) { categoryPairs.toMap() }
    val labelNamesById = remember(labelPairs) { labelPairs.toMap() }
    val isDark = isSystemInDarkTheme()
    val appliedTemplate = remember(templates, appliedTemplateId) { templates.firstOrNull { it.id == appliedTemplateId } }
    val appliedTemplateIcon = appliedTemplate?.let { categoryNamesById[it.categoryId] }?.let { categoryIcon(it) }

    LiquidGlassCard(
        tint = tint,
        modifier = Modifier.fillMaxWidth(),
        lightweight = true,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().clickable { showPicker = true }.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            GlassIconBubble(icon = appliedTemplateIcon ?: Icons.Filled.Bookmark, tint = tint, size = 32.dp)
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                // Matches Account/Category's own label + value treatment: a neutral small label
                // (not tinted), then the value in the card's own raw tint — reading darker than
                // the card's lightened glass surface, same as Category's accent-colored value.
                Text(
                    text = "Template",
                    fontSize = 11.sp,
                    color = if (isDark) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = appliedTemplate?.name ?: "Apply or save a template",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDark) Color.White else tint.darkenForLightCard(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Icon(
                Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(16.dp),
            )
        }
    }

    if (showPicker) {
        Dialog(onDismissRequest = { showPicker = false }) {
            // Same tinted liquid-glass popup as AccountPickerDialog/CategoryPickerDialog: a
            // genuine page blur behind a tinted glass panel, icon-bubble rows, and an "Add" row
            // instead of a separate FAB, so the three pickers read as one component family.
            GlassWindowBlur()
            LiquidGlassCard(
                tint = tint,
                modifier = Modifier.fillMaxWidth().heightIn(max = 560.dp),
                shape = GlassShapes.large,
                cornerRadius = GlassTokens.cornerLarge,
            ) {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
                    ) {
                        GlassIconBubble(icon = Icons.Filled.Bookmark, tint = tint, size = 32.dp)
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "Templates",
                            style = MaterialTheme.typography.titleMedium,
                            color = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f),
                        )
                        IconButton(onClick = { showPicker = false }) {
                            Icon(
                                Icons.Filled.Close,
                                contentDescription = "Close",
                                tint = if (isDark) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    if (templates.isEmpty()) {
                        Text(
                            text = "No templates yet — add one below.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isDark) Color.White.copy(alpha = 0.75f) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                        )
                    } else {
                        LazyColumn {
                            itemsIndexed(templates, key = { _, template -> template.id }) { _, template ->
                                val subtitle = listOfNotNull(
                                    accountNamesById[template.accountId],
                                    categoryNamesById[template.categoryId],
                                    labelNamesById[template.labelId],
                                ).joinToString(" · ")
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onApplyTemplate(template.id)
                                            showPicker = false
                                        }
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                ) {
                                    val rowIcon = categoryNamesById[template.categoryId]?.let { categoryIcon(it) } ?: Icons.Filled.Bookmark
                                    GlassIconBubble(icon = rowIcon, tint = tint, size = 40.dp)
                                    Spacer(Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = template.name,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                        Text(
                                            text = subtitle,
                                            fontSize = 13.sp,
                                            color = if (isDark) Color.White.copy(alpha = 0.75f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                    }
                                    IconButton(onClick = { editingTemplate = template }) {
                                        Icon(
                                            Icons.Filled.Edit,
                                            contentDescription = "Edit ${template.name}",
                                            tint = if (isDark) Color.White.copy(alpha = 0.85f) else tint,
                                        )
                                    }
                                    IconButton(onClick = { pendingDeleteTemplate = template }) {
                                        Icon(
                                            Icons.Filled.Delete,
                                            contentDescription = "Delete ${template.name}",
                                            tint = if (isDark) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showCreateDialog = true }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                    ) {
                        Icon(
                            Icons.Filled.Add,
                            contentDescription = null,
                            tint = if (isDark) Color.White else tint,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Add template",
                            color = if (isDark) Color.White else tint,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        TemplateFormDialog(
            title = "New template",
            accountOptions = accountPairs,
            categoryOptions = categoryPairs,
            labelOptions = labelPairs,
            initialAccountId = currentAccountId,
            initialCategoryId = currentCategoryId,
            initialLabelId = currentLabelIds.firstOrNull(),
            initialPayee = currentPayee,
            initialPlace = currentPlace,
            onConfirm = { name, accountId, categoryId, labelId, payee, place ->
                onCreateTemplate(name, accountId, categoryId, labelId, payee, place)
                showCreateDialog = false
            },
            onDismiss = { showCreateDialog = false },
        )
    }

    val templateToEdit = editingTemplate
    if (templateToEdit != null) {
        TemplateFormDialog(
            title = "Edit template",
            accountOptions = accountPairs,
            categoryOptions = categoryPairs,
            labelOptions = labelPairs,
            initialName = templateToEdit.name,
            initialAccountId = templateToEdit.accountId,
            initialCategoryId = templateToEdit.categoryId,
            initialLabelId = templateToEdit.labelId,
            initialPayee = templateToEdit.payee.orEmpty(),
            initialPlace = templateToEdit.place.orEmpty(),
            onConfirm = { name, accountId, categoryId, labelId, payee, place ->
                onUpdateTemplate(templateToEdit.id, name, accountId, categoryId, labelId, payee, place)
                editingTemplate = null
            },
            onDismiss = { editingTemplate = null },
        )
    }

    val templateToDelete = pendingDeleteTemplate
    if (templateToDelete != null) {
        ConfirmationDialog(
            title = "Delete this template?",
            message = "\"${templateToDelete.name}\" will no longer be available when adding a transaction.",
            confirmLabel = "Delete",
            onConfirm = {
                onDeleteTemplate(templateToDelete.id)
                pendingDeleteTemplate = null
            },
            onDismiss = { pendingDeleteTemplate = null },
        )
    }
}

/** A page-2 row: leading tinted icon bubble, tinted small label, inline-editable value and a
 * trailing tinted chevron (Add-Transaction-Reference) — Note/Payee/Place all share this shape.
 * The card itself is tinted liquid glass rather than neutral, so each row carries its own
 * color wash in both themes. */
@Composable
private fun GlassTextRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    label: String,
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    suggestions: List<String> = emptyList(),
) {
    var isFocused by remember { mutableStateOf(false) }
    // Recent/recurring values (Payee, Place) minus whatever's already typed and an exact match —
    // once the field holds one of these verbatim there's nothing left to suggest.
    val visibleSuggestions = remember(suggestions, value, isFocused) {
        if (!isFocused || suggestions.isEmpty()) {
            emptyList()
        } else {
            suggestions.filter { it.contains(value, ignoreCase = true) && !it.equals(value, ignoreCase = true) }
        }
    }
    LiquidGlassCard(
        tint = tint,
        modifier = Modifier.fillMaxWidth(),
        lightweight = true,
    ) {
        Column {
        Row(
            modifier = Modifier.heightIn(min = DetailRowMinHeight).padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            GlassIconBubble(icon = icon, tint = tint, size = 32.dp)
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = label, fontSize = 11.sp, color = tint)
                TextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    textStyle = TextStyle(fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface),
                    placeholder = { Text(placeholder, fontSize = 15.sp) },
                    colors = transparentTextFieldColors,
                    modifier = Modifier.fillMaxWidth().onFocusChanged { isFocused = it.isFocused },
                )
            }
            Spacer(Modifier.width(8.dp))
            Icon(
                Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(16.dp),
            )
        }
        if (visibleSuggestions.isNotEmpty()) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(start = 54.dp, end = 12.dp, bottom = 12.dp),
            ) {
                visibleSuggestions.forEach { suggestion ->
                    LabelChip(name = suggestion, onClick = { onValueChange(suggestion) })
                }
            }
        }
        }
    }
}

@Composable
private fun GlassDateRow(tint: Color, dateMillis: Long, onDateChange: (Long) -> Unit) {
    var showPicker by remember { mutableStateOf(false) }
    val formatted = remember(dateMillis) {
        SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(dateMillis))
    }
    LiquidGlassCard(
        tint = tint,
        modifier = Modifier.fillMaxWidth(),
        lightweight = true,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showPicker = true }
                .heightIn(min = DetailRowMinHeight)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            GlassIconBubble(icon = Icons.Filled.CalendarMonth, tint = tint, size = 32.dp)
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Date", fontSize = 11.sp, color = tint)
                Text(text = formatted, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
            }
            Spacer(Modifier.width(8.dp))
            Icon(
                Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(16.dp),
            )
        }
    }
    if (showPicker) {
        GlassDatePickerDialog(
            initialDateMillis = dateMillis,
            onDismiss = { showPicker = false },
            onConfirm = onDateChange,
        )
    }
}

@Composable
private fun GlassTimeRow(tint: Color, dateMillis: Long, onTimeChange: (Long) -> Unit) {
    var showPicker by remember { mutableStateOf(false) }
    val formatted = remember(dateMillis) {
        SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(dateMillis))
    }
    LiquidGlassCard(
        tint = tint,
        modifier = Modifier.fillMaxWidth(),
        lightweight = true,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showPicker = true }
                .heightIn(min = DetailRowMinHeight)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            GlassIconBubble(icon = Icons.Filled.AccessTime, tint = tint, size = 32.dp)
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Time", fontSize = 11.sp, color = tint)
                Text(text = formatted, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
            }
            Spacer(Modifier.width(8.dp))
            Icon(
                Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(16.dp),
            )
        }
    }
    if (showPicker) {
        GlassTimePickerDialog(
            initialDateMillis = dateMillis,
            onDismiss = { showPicker = false },
            onConfirm = onTimeChange,
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
    onCreateLabel: (name: String) -> Unit,
) {
    var showPicker by remember { mutableStateOf(false) }
    var showCreateLabel by remember { mutableStateOf(false) }
    val selectedLabels = allLabels.filter { it.id in selectedLabelIds }

    LiquidGlassCard(
        tint = tint,
        modifier = Modifier.fillMaxWidth(),
        lightweight = true,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showPicker = true }
                .heightIn(min = DetailRowMinHeight)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            GlassIconBubble(icon = Icons.Filled.Sell, tint = tint, size = 32.dp)
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Labels", fontSize = 11.sp, color = tint)
                if (selectedLabels.isEmpty()) {
                    Text(text = "Add label (optional)", fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.horizontalScroll(rememberScrollState()).padding(top = 2.dp),
                    ) {
                        selectedLabels.forEach { label -> LabelChip(name = label.name) }
                    }
                }
            }
            Spacer(Modifier.width(8.dp))
            Icon(
                Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(16.dp),
            )
        }
    }

    if (showPicker) {
        // A custom Dialog+Surface rather than AlertDialog — AlertDialog's fixed title/text/button
        // section padding leaves a large empty gap under a short list like this one (a handful of
        // labels, or none). This hugs the content instead.
        Dialog(onDismissRequest = { showPicker = false }) {
            GlassWindowBlur()
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = glassDialogContainerColor(GlassStyle.Thick),
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(text = "Labels", style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(16.dp))
                    if (allLabels.isEmpty()) {
                        Text("No labels yet — create your first one below.")
                        Spacer(Modifier.height(4.dp))
                    } else {
                        allLabels.forEach { label ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onToggleLabel(label.id) }
                                    .padding(vertical = 4.dp),
                            ) {
                                Checkbox(checked = label.id in selectedLabelIds, onCheckedChange = { onToggleLabel(label.id) })
                                Spacer(Modifier.width(8.dp))
                                LabelChip(name = label.name)
                            }
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showCreateLabel = true }
                            .padding(vertical = 10.dp),
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Add label", color = tint)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showPicker = false }) { Text("Done") }
                    }
                }
            }
        }
    }

    if (showCreateLabel) {
        LabelFormDialog(
            title = "New label",
            onConfirm = { name ->
                onCreateLabel(name)
                showCreateLabel = false
            },
            onDismiss = { showCreateLabel = false },
        )
    }
}
