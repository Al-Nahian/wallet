package com.example.wallet.feature.labels

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import com.example.wallet.core.design.components.GlassScreenScaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.wallet.core.design.components.ConfirmationDialog
import com.example.wallet.core.design.components.EmptyState
import com.example.wallet.core.design.components.GlassScreenTopBar
import com.example.wallet.core.design.components.LabelChip
import com.example.wallet.core.design.glass.GlassFab
import com.example.wallet.core.design.glass.GlassStyle
import com.example.wallet.core.design.glass.GlassWindowBlur
import com.example.wallet.core.design.glass.glassDialogContainerColor
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

    GlassScreenScaffold(
        modifier = modifier,
        topBar = {
            GlassScreenTopBar(
                title = "Labels",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        floatingActionButton = {
            GlassFab(onClick = { showCreateDialog = true }, contentDescription = "Add label") {
                Icon(Icons.Filled.Add, contentDescription = null)
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
                        LabelChip(name = label.name)
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
            onConfirm = { name ->
                viewModel.createLabel(name)
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
            onConfirm = { name ->
                viewModel.updateLabel(labelToEdit.id, name)
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

/** A custom Dialog+Surface rather than AlertDialog — AlertDialog's fixed title/text/button
 * section padding leaves a large empty gap under content this short (just one text field, now
 * that labels carry no color picker). This hugs the content instead. */
@Composable
fun LabelFormDialog(
    title: String,
    onConfirm: (name: String) -> Unit,
    onDismiss: () -> Unit,
    initialName: String = "",
) {
    var name by remember { mutableStateOf(initialName) }

    Dialog(onDismissRequest = onDismiss) {
        GlassWindowBlur()
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = glassDialogContainerColor(GlassStyle.Thick),
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(text = title, style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Label name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    TextButton(onClick = { onConfirm(name) }, enabled = name.isNotBlank()) { Text("Save") }
                }
            }
        }
    }
}
