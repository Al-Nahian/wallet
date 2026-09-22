package com.example.wallet.feature.labels

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.wallet.core.design.components.ConfirmationDialog
import com.example.wallet.core.design.components.EmptyState
import com.example.wallet.core.design.components.LabelChip
import com.example.wallet.core.design.parseHexColor
import com.example.wallet.domain.model.Label

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LabelsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LabelsViewModel = hiltViewModel(),
) {
    val labels by viewModel.labels.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var showCreateDialog by remember { mutableStateOf(false) }
    var editLabel by remember { mutableStateOf<Label?>(null) }
    var pendingDeleteLabel by remember { mutableStateOf<Label?>(null) }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.dismissError()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Labels") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Add label")
            }
        },
    ) { paddingValues ->
        if (labels.isEmpty()) {
            EmptyState(
                title = "No labels yet",
                subtitle = "Create labels like Family or Work to tag your transactions.",
                actionLabel = "Add label",
                onAction = { showCreateDialog = true },
                modifier = Modifier.padding(paddingValues).fillMaxSize(),
            )
        } else {
            LazyColumn(modifier = Modifier.padding(paddingValues).fillMaxWidth()) {
                items(labels, key = { it.id }) { label ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { editLabel = label }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        LabelChip(name = label.name, color = label.color)
                        IconButton(onClick = { pendingDeleteLabel = label }) {
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = "Delete ${label.name}",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        LabelFormDialog(
            title = "New label",
            onConfirm = { name, color ->
                viewModel.createLabel(name, color)
                showCreateDialog = false
            },
            onDismiss = { showCreateDialog = false },
        )
    }

    val labelToEdit = editLabel
    if (labelToEdit != null) {
        LabelFormDialog(
            title = "Edit label",
            initialName = labelToEdit.name,
            initialColor = labelToEdit.color,
            onConfirm = { name, color ->
                viewModel.updateLabel(labelToEdit.id, name, color)
                editLabel = null
            },
            onDismiss = { editLabel = null },
        )
    }

    val labelToDelete = pendingDeleteLabel
    if (labelToDelete != null) {
        ConfirmationDialog(
            title = "Delete this label?",
            message = "\"${labelToDelete.name}\" will be removed from every transaction it's on.",
            confirmLabel = "Delete",
            onConfirm = {
                viewModel.deleteLabel(labelToDelete.id)
                pendingDeleteLabel = null
            },
            onDismiss = { pendingDeleteLabel = null },
        )
    }
}

@Composable
private fun LabelFormDialog(
    title: String,
    onConfirm: (name: String, color: String) -> Unit,
    onDismiss: () -> Unit,
    initialName: String = "",
    initialColor: String = LabelColorPalette.first(),
) {
    var name by remember { mutableStateOf(initialName) }
    var color by remember { mutableStateOf(initialColor) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Label name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = "Color",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    LabelColorPalette.forEach { swatch ->
                        val isSelected = swatch == color
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(parseHexColor(swatch))
                                .border(
                                    width = if (isSelected) 3.dp else 0.dp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    shape = CircleShape,
                                )
                                .clickable { color = swatch },
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(name, color) }, enabled = name.isNotBlank()) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}
