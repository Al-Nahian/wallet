package com.example.wallet.core.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.wallet.core.design.glass.GlassSheet
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
            onSelected = { id ->
                onSelected(id)
                showPicker = false
            },
            onDismiss = { showPicker = false },
        )
    }
}

@Composable
internal fun CategoryPickerDialog(
    groups: List<CategoryGroup>,
    categories: List<Category>,
    onSelected: (String?) -> Unit,
    onDismiss: () -> Unit,
) {
    var expandedGroup by remember { mutableStateOf<CategoryGroup?>(null) }
    val categoriesByGroup = remember(categories) { categories.groupBy { it.groupId } }

    Dialog(onDismissRequest = onDismiss) {
        GlassSheet(
            modifier = Modifier.fillMaxWidth().heightIn(max = 560.dp),
        ) {
            val group = expandedGroup
            androidx.compose.foundation.layout.Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = if (group != null) 4.dp else 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
                ) {
                    if (group != null) {
                        IconButton(onClick = { expandedGroup = null }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                    Text(text = group?.name ?: "Category", style = MaterialTheme.typography.titleMedium)
                }
                Divider()
                LazyColumn {
                    if (group == null) {
                        item {
                            ListItem(
                                headlineContent = { Text("Uncategorized") },
                                modifier = Modifier.clickable { onSelected(null) },
                            )
                        }
                        items(groups, key = { it.id }) { categoryGroup ->
                            ListItem(
                                leadingContent = {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clip(CircleShape)
                                            .background(parseHexColor(categoryGroup.color)),
                                    )
                                },
                                headlineContent = { Text(categoryGroup.name) },
                                trailingContent = { Icon(Icons.Filled.ChevronRight, contentDescription = null) },
                                modifier = Modifier.clickable { expandedGroup = categoryGroup },
                            )
                        }
                    } else {
                        items(categoriesByGroup[group.id].orEmpty(), key = { it.id }) { category ->
                            ListItem(
                                headlineContent = { Text(category.name) },
                                modifier = Modifier.clickable { onSelected(category.id) },
                            )
                        }
                    }
                }
            }
        }
    }
}
