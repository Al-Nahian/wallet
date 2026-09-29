package com.example.wallet.core.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.wallet.core.design.WalletTheme
import com.example.wallet.core.design.glass.GlassShapes
import com.example.wallet.core.design.glass.GlassTokens
import com.example.wallet.core.design.glass.GlassWindowBlur
import com.example.wallet.core.design.parseHexColor
import com.example.wallet.domain.model.Category
import com.example.wallet.domain.model.CategoryGroup

/**
 * A two-step category picker (plan.md §14): tapping the field opens a dialog that first lists
 * parent [CategoryGroup]s, then — on tapping one — that group's subcategories. Only a
 * subcategory (leaf [Category]) is selectable; a parent group is never itself a valid category,
 * it only navigates one level deeper. Replaces the old single flat dropdown.
 */
@Composable
fun CategoryPickerField(
    groups: List<CategoryGroup>,
    categories: List<Category>,
    selectedCategoryId: String?,
    onSelected: (String?) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Category",
) {
    var showPicker by remember { mutableStateOf(false) }
    val selectedLabel = categories.firstOrNull { it.id == selectedCategoryId }?.name ?: "Uncategorized"

    Box(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = selectedLabel,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { showPicker = true },
        )
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

/** Centered glass popup styled to match [com.example.wallet.feature.transactions.AccountPickerDialog]
 * — a tinted [LiquidGlassCard] panel behind a genuine page blur, with each row carrying a
 * colored icon bubble and the selected row highlighted as a pill with a check bubble. First
 * level lists [CategoryGroup]s (plus "Uncategorized"); tapping one drills into its subcategories,
 * which is where a leaf [Category] can actually be selected. */
@Composable
internal fun CategoryPickerDialog(
    groups: List<CategoryGroup>,
    categories: List<Category>,
    selectedCategoryId: String? = null,
    onSelected: (String?) -> Unit,
    onDismiss: () -> Unit,
) {
    var expandedGroup by remember { mutableStateOf<CategoryGroup?>(null) }
    val categoriesByGroup = remember(categories) { categories.groupBy { it.groupId } }
    val accent = WalletTheme.extendedColors.accent
    val isDark = isSystemInDarkTheme()
    val group = expandedGroup

    Dialog(onDismissRequest = onDismiss) {
        GlassWindowBlur()
        LiquidGlassCard(
            tint = accent,
            modifier = Modifier.fillMaxWidth().heightIn(max = 560.dp),
            shape = GlassShapes.large,
            cornerRadius = GlassTokens.cornerLarge,
        ) {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = if (group != null) 4.dp else 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
                ) {
                    if (group != null) {
                        IconButton(onClick = { expandedGroup = null }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface,
                            )
                        }
                        Spacer(Modifier.width(2.dp))
                    } else {
                        GlassIconBubble(icon = Icons.Filled.Sell, tint = accent, size = 32.dp)
                        Spacer(Modifier.width(10.dp))
                    }
                    Text(
                        text = group?.name ?: "Category",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = "Close",
                            tint = if (isDark) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                LazyColumn {
                    if (group == null) {
                        items(groups, key = { it.id }) { categoryGroup ->
                            val count = categoriesByGroup[categoryGroup.id].orEmpty().size
                            CategoryPickerRow(
                                icon = categoryGroupIcon(categoryGroup.name),
                                iconTint = parseHexColor(categoryGroup.color),
                                name = categoryGroup.name,
                                subtitle = if (count == 1) "1 category" else "$count categories",
                                selected = false,
                                isDark = isDark,
                                trailing = {
                                    Icon(
                                        Icons.Filled.ChevronRight,
                                        contentDescription = null,
                                        tint = if (isDark) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                },
                                onClick = { expandedGroup = categoryGroup },
                            )
                        }
                    } else {
                        items(categoriesByGroup[group.id].orEmpty(), key = { it.id }) { category ->
                            CategoryPickerRow(
                                icon = categoryIcon(category.name),
                                iconTint = parseHexColor(group.color),
                                name = category.name,
                                subtitle = null,
                                selected = category.id == selectedCategoryId,
                                isDark = isDark,
                                onClick = { onSelected(category.id) },
                            )
                        }
                    }
                }
            }
        }
    }
}

/** One row in [CategoryPickerDialog] — mirrors AccountPickerDialog's row treatment (icon bubble,
 * name + optional subtitle, selected state as a highlighted pill with a check bubble) so both
 * pickers read as the same component family. */
@Composable
private fun CategoryPickerRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    name: String,
    subtitle: String?,
    selected: Boolean,
    isDark: Boolean,
    onClick: () -> Unit,
    trailing: (@Composable () -> Unit)? = null,
) {
    val accent = WalletTheme.extendedColors.accent
    val row = @Composable {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
        ) {
            GlassIconBubble(icon = icon, tint = iconTint, size = 40.dp)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        fontSize = 13.sp,
                        color = if (isDark) Color.White.copy(alpha = 0.75f) else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
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
            } else {
                trailing?.invoke()
            }
        }
    }
    if (selected) {
        LiquidGlassCard(
            tint = accent,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 2.dp)
                .clickable(onClick = onClick),
            shape = GlassShapes.pill,
            cornerRadius = 999.dp,
            lightweight = true,
        ) {
            Box(modifier = Modifier.fillMaxWidth()) { row() }
        }
    } else {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 2.dp)
                .clickable(onClick = onClick),
        ) {
            row()
        }
    }
}
