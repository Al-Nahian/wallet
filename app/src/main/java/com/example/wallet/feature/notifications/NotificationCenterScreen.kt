package com.example.wallet.feature.notifications

import android.text.format.DateUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.wallet.core.design.components.EmptyState
import com.example.wallet.core.design.components.GlassScreenTopBar
import com.example.wallet.domain.model.Notification

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationCenterScreen(
    onBack: () -> Unit,
    onDeepLink: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NotificationCenterViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        topBar = {
            GlassScreenTopBar(
                title = "Notifications",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { paddingValues ->
        when (val state = uiState) {
            NotificationCenterUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            is NotificationCenterUiState.Loaded -> {
                if (state.all.isEmpty()) {
                    EmptyState(
                        title = "No notifications yet",
                        subtitle = "Updates about your accounts and budgets will show up here.",
                        icon = Icons.Filled.Notifications,
                        modifier = Modifier.padding(paddingValues),
                    )
                } else {
                    LazyColumn(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
                        if (state.needsAttention.isNotEmpty()) {
                            item { SectionHeader("Needs your attention") }
                            items(state.needsAttention, key = { "attention-${it.id}" }) { notification ->
                                NotificationRow(notification) {
                                    viewModel.onNotificationClicked(notification)
                                    notification.deepLink?.let(onDeepLink)
                                }
                            }
                        }
                        item { SectionHeader("All") }
                        items(state.all, key = { it.id }) { notification ->
                            NotificationRow(notification) {
                                viewModel.onNotificationClicked(notification)
                                notification.deepLink?.let(onDeepLink)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
    )
}

@Composable
private fun NotificationRow(notification: Notification, onClick: () -> Unit) {
    ListItem(
        modifier = Modifier
            .clickable(onClick = onClick)
            .background(
                if (notification.isUnread) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.06f)
                } else {
                    MaterialTheme.colorScheme.surface
                },
            ),
        headlineContent = {
            Text(
                text = notification.title,
                fontWeight = if (notification.isUnread) FontWeight.Bold else FontWeight.Normal,
            )
        },
        supportingContent = { Text(notification.body) },
        trailingContent = {
            Text(
                text = DateUtils.getRelativeTimeSpanString(
                    notification.createdAt,
                    System.currentTimeMillis(),
                    DateUtils.MINUTE_IN_MILLIS,
                ).toString(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
    )
}
