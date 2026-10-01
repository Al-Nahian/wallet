package com.expensetracker.wallet.feature.categories

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import com.expensetracker.wallet.core.design.components.GlassScreenScaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.expensetracker.wallet.core.design.components.CategoryIcon
import com.expensetracker.wallet.core.design.components.ConfirmationDialog
import com.expensetracker.wallet.core.design.components.GlassScreenTopBar
import com.expensetracker.wallet.core.design.components.TextInputDialog
import com.expensetracker.wallet.core.design.parseHexColor
import com.expensetracker.wallet.domain.model.Category

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CategoriesViewModel = hiltViewModel(),
) {
    val groups by viewModel.groups.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var addCategoryGroupId by remember { mutableStateOf<String?>(null) }
    var editCategory by remember { mutableStateOf<Category?>(null) }
    var pendingDeleteCategory by remember { mutableStateOf<Category?>(null) }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.dismissError()
        }
    }

    GlassScreenScaffold(
        modifier = modifier,
        topBar = {
            GlassScreenTopBar(
                title = "Categories",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { paddingValues ->
        LazyColumn(modifier = Modifier.padding(paddingValues).fillMaxWidth()) {
            groups.forEach { groupUi ->
                item(key = "header-${groupUi.group.id}") {
                    CategoryGroupHeader(
                        name = groupUi.group.name,
                        color = groupUi.group.color,
                        onAddCategory = { addCategoryGroupId = groupUi.group.id },
                    )
                }
                items(groupUi.categories, key = { it.id }) { category ->
                    CategoryRow(
                        category = category,
                        groupColor = groupUi.group.color,
                        onClick = {
                            if (category.isSystem) {
                                Toast.makeText(context, "Default categories can't be renamed.", Toast.LENGTH_SHORT).show()
                            } else {
                                editCategory = category
                            }
                        },
                        onDelete = { pendingDeleteCategory = category },
                    )
                }
            }
        }
    }

    val addGroupId = addCategoryGroupId
    if (addGroupId != null) {
        TextInputDialog(
            title = "New category",
            label = "Category name",
            confirmLabel = "Add",
            onConfirm = { name ->
                viewModel.createCategory(addGroupId, name)
                addCategoryGroupId = null
            },
            onDismiss = { addCategoryGroupId = null },
        )
    }

    val categoryToEdit = editCategory
    if (categoryToEdit != null) {
        TextInputDialog(
            title = "Rename category",
            label = "Category name",
            initialValue = categoryToEdit.name,
            onConfirm = { name ->
                viewModel.renameCategory(categoryToEdit.id, name)
                editCategory = null
            },
            onDismiss = { editCategory = null },
        )
    }

    val categoryToDelete = pendingDeleteCategory
    if (categoryToDelete != null) {
        ConfirmationDialog(
            title = "Delete this category?",
            message = "\"${categoryToDelete.name}\" will be removed. This only works if no transaction uses it.",
            confirmLabel = "Delete",
            onConfirm = {
                viewModel.deleteCategory(categoryToDelete.id)
                pendingDeleteCategory = null
            },
            onDismiss = { pendingDeleteCategory = null },
        )
    }
}

@Composable
private fun CategoryGroupHeader(name: String, color: String, onAddCategory: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(parseHexColor(color)),
            )
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
        IconButton(onClick = onAddCategory) {
            Icon(Icons.Filled.Add, contentDescription = "Add category to $name")
        }
    }
}

@Composable
private fun CategoryRow(category: Category, groupColor: String, onClick: () -> Unit, onDelete: () -> Unit) {
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        leadingContent = { CategoryIcon(name = category.name, color = groupColor, size = 32.dp) },
        headlineContent = { Text(category.name) },
        supportingContent = if (category.isSystem) {
            { Text("Default", style = MaterialTheme.typography.bodySmall) }
        } else {
            null
        },
        trailingContent = if (!category.isSystem) {
            {
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = "Delete ${category.name}",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else {
            null
        },
    )
}
