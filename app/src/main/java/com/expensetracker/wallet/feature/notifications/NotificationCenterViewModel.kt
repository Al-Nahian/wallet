package com.expensetracker.wallet.feature.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expensetracker.wallet.domain.model.Notification
import com.expensetracker.wallet.domain.model.needsAttention
import com.expensetracker.wallet.domain.repository.NotificationRepository
import com.expensetracker.wallet.domain.usecase.notification.MarkAllNotificationsReadUseCase
import com.expensetracker.wallet.domain.usecase.notification.MarkNotificationReadUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface NotificationCenterUiState {
    data object Loading : NotificationCenterUiState
    data class Loaded(
        val needsAttention: List<Notification>,
        val all: List<Notification>,
    ) : NotificationCenterUiState
}

@HiltViewModel
class NotificationCenterViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val markNotificationReadUseCase: MarkNotificationReadUseCase,
    private val markAllNotificationsReadUseCase: MarkAllNotificationsReadUseCase,
) : ViewModel() {

    init {
        // Opening Notification Center is itself the "seen" signal — clears the bell's badge.
        viewModelScope.launch { markAllNotificationsReadUseCase() }
    }

    val uiState: StateFlow<NotificationCenterUiState> = notificationRepository.observeAll()
        .map { notifications ->
            NotificationCenterUiState.Loaded(
                needsAttention = notifications.filter { it.isUnread && it.type.needsAttention() },
                all = notifications,
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = NotificationCenterUiState.Loading,
        )

    /** Marks the notification read; the caller navigates via its `deepLink` separately. */
    fun onNotificationClicked(notification: Notification) {
        if (notification.isUnread) {
            viewModelScope.launch { markNotificationReadUseCase(notification.id) }
        }
    }
}
