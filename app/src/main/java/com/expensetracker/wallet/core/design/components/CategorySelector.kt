package com.expensetracker.wallet.core.design.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** `selectedId = null` renders as "Uncategorized" — category is optional (plan.md §21). */
@Composable
fun CategorySelector(
    categories: List<SelectorOption>,
    selectedCategoryId: String?,
    onSelected: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val uncategorizedId = ""
    val options = listOf(SelectorOption(uncategorizedId, "Uncategorized")) + categories

    SelectorField(
        label = "Category",
        options = options,
        selectedId = selectedCategoryId ?: uncategorizedId,
        onSelected = { id -> onSelected(id.ifEmpty { null }) },
        modifier = modifier,
    )
}
