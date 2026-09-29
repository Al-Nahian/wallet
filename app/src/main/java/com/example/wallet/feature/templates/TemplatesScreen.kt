package com.example.wallet.feature.templates

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import com.example.wallet.core.design.components.GlassIconBubble
import com.example.wallet.core.design.components.GlassScreenScaffold
import com.example.wallet.core.design.components.GlassScreenTopBar
import com.example.wallet.core.design.components.categoryIcon
import com.example.wallet.core.design.DarkPrimary
import com.example.wallet.core.design.glass.GlassFab
import com.example.wallet.core.design.glass.GlassStyle
import com.example.wallet.core.design.glass.GlassWindowBlur
import com.example.wallet.core.design.glass.glassDialogContainerColor
import com.example.wallet.domain.model.Template

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplatesScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TemplatesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var showCreateDialog by remember { mutableStateOf(false) }
    var editTemplate by remember { mutableStateOf<Template?>(null) }
    var pendingDeleteTemplate by remember { mutableStateOf<Template?>(null) }

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
                title = "Templates",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        floatingActionButton = {
            GlassFab(onClick = { showCreateDialog = true }, contentDescription = "Add template") {
                Icon(Icons.Filled.Add, contentDescription = null)
            }
        },
    ) { paddingValues ->
        if (uiState.templates.isEmpty()) {
            EmptyState(
                title = "No templates yet",
                subtitle = "Save a fixed account, category, and label — like \"Lunch\": Wallet + " +
                    "Food + Food — to fill them in with one tap when adding a transaction.",
                actionLabel = "Add template",
                onAction = { showCreateDialog = true },
                modifier = Modifier.padding(paddingValues).fillMaxSize(),
            )
        } else {
            val accountsById = uiState.accounts.associateBy { it.id }
            val categoriesById = uiState.categories.associateBy { it.id }
            val labelsById = uiState.labels.associateBy { it.id }
            LazyColumn(modifier = Modifier.padding(paddingValues).fillMaxWidth()) {
                items(uiState.templates, key = { it.id }) { template ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { editTemplate = template }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        val accountName = accountsById[template.accountId]?.name ?: "Unknown account"
                        val categoryName = categoriesById[template.categoryId]?.name ?: "Unknown category"
                        val labelName = labelsById[template.labelId]?.name ?: "Unknown label"
                        GlassIconBubble(
                            icon = categoryIcon(categoryName),
                            tint = DarkPrimary,
                            size = 40.dp,
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = template.name, style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = "$accountName · $categoryName · $labelName",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        IconButton(onClick = { pendingDeleteTemplate = template }) {
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = "Delete ${template.name}",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }

    val accountOptions = uiState.accounts.map { it.id to it.name }
    val categoryOptions = uiState.categories.map { it.id to it.name }
    val labelOptions = uiState.labels.map { it.id to it.name }

    if (showCreateDialog) {
        TemplateFormDialog(
            title = "New template",
            accountOptions = accountOptions,
            categoryOptions = categoryOptions,
            labelOptions = labelOptions,
            onConfirm = { name, accountId, categoryId, labelId, payee, place ->
                viewModel.createTemplate(name, accountId, categoryId, labelId, payee, place)
                showCreateDialog = false
            },
            onDismiss = { showCreateDialog = false },
        )
    }

    val templateToEdit = editTemplate
    if (templateToEdit != null) {
        TemplateFormDialog(
            title = "Edit template",
            accountOptions = accountOptions,
            categoryOptions = categoryOptions,
            labelOptions = labelOptions,
            initialName = templateToEdit.name,
            initialAccountId = templateToEdit.accountId,
            initialCategoryId = templateToEdit.categoryId,
            initialLabelId = templateToEdit.labelId,
            initialPayee = templateToEdit.payee.orEmpty(),
            initialPlace = templateToEdit.place.orEmpty(),
            onConfirm = { name, accountId, categoryId, labelId, payee, place ->
                viewModel.updateTemplate(templateToEdit.id, name, accountId, categoryId, labelId, payee, place)
                editTemplate = null
            },
            onDismiss = { editTemplate = null },
        )
    }

    val templateToDelete = pendingDeleteTemplate
    if (templateToDelete != null) {
        ConfirmationDialog(
            title = "Delete this template?",
            message = "\"${templateToDelete.name}\" will no longer be available when adding a transaction.",
            confirmLabel = "Delete",
            onConfirm = {
                viewModel.deleteTemplate(templateToDelete.id)
                pendingDeleteTemplate = null
            },
            onDismiss = { pendingDeleteTemplate = null },
        )
    }
}

/** A custom Dialog+Surface (see the calendar/label pickers for the same pattern) rather than
 * AlertDialog, so the account/label sub-pickers and optional payee/place fields don't leave the
 * generous empty padding AlertDialog reserves under short content. Shared with the transaction
 * form's own "Template" card, which can add/edit a template inline without leaving the form. */
@Composable
fun TemplateFormDialog(
    title: String,
    accountOptions: List<Pair<String, String>>,
    categoryOptions: List<Pair<String, String>>,
    labelOptions: List<Pair<String, String>>,
    onConfirm: (name: String, accountId: String?, categoryId: String?, labelId: String?, payee: String?, place: String?) -> Unit,
    onDismiss: () -> Unit,
    initialName: String = "",
    initialAccountId: String? = null,
    initialCategoryId: String? = null,
    initialLabelId: String? = null,
    initialPayee: String = "",
    initialPlace: String = "",
) {
    var name by remember { mutableStateOf(initialName) }
    var accountId by remember { mutableStateOf(initialAccountId) }
    var categoryId by remember { mutableStateOf(initialCategoryId) }
    var labelId by remember { mutableStateOf(initialLabelId) }
    var payee by remember { mutableStateOf(initialPayee) }
    var place by remember { mutableStateOf(initialPlace) }
    var showAccountPicker by remember { mutableStateOf(false) }
    var showCategoryPicker by remember { mutableStateOf(false) }
    var showLabelPicker by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        GlassWindowBlur()
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = glassDialogContainerColor(GlassStyle.Thick),
        ) {
            Column(modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState())) {
                Text(text = title, style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Template name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(12.dp))
                TemplatePickerField(
                    label = "Account",
                    value = accountOptions.firstOrNull { it.first == accountId }?.second ?: "Select account",
                    onClick = { showAccountPicker = true },
                )
                Spacer(Modifier.height(8.dp))
                TemplatePickerField(
                    label = "Category",
                    value = categoryOptions.firstOrNull { it.first == categoryId }?.second ?: "Select category",
                    onClick = { showCategoryPicker = true },
                )
                Spacer(Modifier.height(8.dp))
                TemplatePickerField(
                    label = "Label",
                    value = labelOptions.firstOrNull { it.first == labelId }?.second ?: "Select label",
                    onClick = { showLabelPicker = true },
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = payee,
                    onValueChange = { payee = it },
                    label = { Text("Payee (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = place,
                    onValueChange = { place = it },
                    label = { Text("Place (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    TextButton(
                        onClick = { onConfirm(name, accountId, categoryId, labelId, payee, place) },
                        enabled = name.isNotBlank() && accountId != null && categoryId != null && labelId != null,
                    ) { Text("Save") }
                }
            }
        }
    }

    if (showAccountPicker) {
        TemplateOptionPickerDialog(
            title = "Select account",
            options = accountOptions,
            onSelected = { accountId = it },
            onDismiss = { showAccountPicker = false },
        )
    }
    if (showCategoryPicker) {
        TemplateOptionPickerDialog(
            title = "Select category",
            options = categoryOptions,
            onSelected = { categoryId = it },
            onDismiss = { showCategoryPicker = false },
        )
    }
    if (showLabelPicker) {
        TemplateOptionPickerDialog(
            title = "Select label",
            options = labelOptions,
            onSelected = { labelId = it },
            onDismiss = { showLabelPicker = false },
        )
    }
}

@Composable
private fun TemplatePickerField(label: String, value: String, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = value, style = MaterialTheme.typography.bodyLarge)
            }
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun TemplateOptionPickerDialog(
    title: String,
    options: List<Pair<String, String>>,
    onSelected: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        GlassWindowBlur()
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = glassDialogContainerColor(GlassStyle.Thick),
        ) {
            Column(modifier = Modifier.padding(vertical = 16.dp).heightIn(max = 480.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                )
                if (options.isEmpty()) {
                    Text(
                        text = "Nothing to pick from yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                    )
                } else {
                    LazyColumn {
                        items(options, key = { it.first }) { (id, optionName) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onSelected(id)
                                        onDismiss()
                                    }
                                    .padding(horizontal = 24.dp, vertical = 14.dp),
                            ) {
                                Text(text = optionName, style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
                }
            }
        }
    }
}
