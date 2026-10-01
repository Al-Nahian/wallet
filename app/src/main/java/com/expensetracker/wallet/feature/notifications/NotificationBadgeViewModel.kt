package com.expensetracker.wallet.feature.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expensetracker.wallet.domain.usecase.notification.ObserveUnreadNotificationCountUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/** Feeds the bell icon's unread-count badge from every top-level screen's shared top bar. */
@HiltViewModel
class NotificationBadgeViewModel @Inject constructor(
    observeUnreadNotificationCount: ObserveUnreadNotificationCountUseCase,
) : ViewModel() {

    val unreadCount: StateFlow<Int> = observeUnreadNotificationCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)
}
