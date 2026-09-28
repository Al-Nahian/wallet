package com.example.wallet.core.design.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/**
 * Thin wrapper around [Scaffold] so every screen picks up consistent
 * top bar / bottom nav / FAB slots without repeating boilerplate.
 */
@Composable
fun WalletScaffold(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    // Transparent for callers painting their own background behind this Scaffold (the top-level
    // screens' dark-mode glow gradient) — Scaffold's own opaque fill would otherwise hide it.
    containerColor: Color = MaterialTheme.colorScheme.background,
    content: @Composable (paddingValues: androidx.compose.foundation.layout.PaddingValues) -> Unit,
) {
    Scaffold(
        modifier = modifier,
        topBar = topBar,
        bottomBar = bottomBar,
        floatingActionButton = floatingActionButton,
        containerColor = containerColor,
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets,
        content = content,
    )
}
