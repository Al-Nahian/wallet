package com.expensetracker.wallet.core.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

/** A pill showing a label's name — flat and neutral, unlike a category chip: color is a
 * category-only concept (plan.md §69 rule 8), so a label never carries or shows one. [onClick]
 * toggles selection in a picker; [onRemove] shows a trailing remove affordance when set. */
@Composable
fun LabelChip(
    name: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    onRemove: (() -> Unit)? = null,
) {
    val rowModifier = if (onClick != null) modifier.clickable(onClick = onClick) else modifier

    Row(
        modifier = rowModifier
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = name, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (onRemove != null) {
            Spacer(Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "Remove $name",
                modifier = Modifier.size(14.dp).clickable(onClick = onRemove),
            )
        }
    }
}
