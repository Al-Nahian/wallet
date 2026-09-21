package com.example.wallet.core.design.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun AccountSelector(
    accounts: List<SelectorOption>,
    selectedAccountId: String?,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    SelectorField(
        label = "Account",
        options = accounts,
        selectedId = selectedAccountId,
        onSelected = onSelected,
        modifier = modifier,
        placeholder = "Select an account",
    )
}
