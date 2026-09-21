package com.example.wallet.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wallet.domain.model.User
import com.example.wallet.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/**
 * `currentUser == null` is guest mode (plan.md §85) — always the case until Phase 16 makes
 * sign-in real. The three sign-in buttons are wired but inert until then (plans/04-...md).
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    userRepository: UserRepository,
) : ViewModel() {

    val currentUser: StateFlow<User?> = userRepository.observeCurrentUser()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}
