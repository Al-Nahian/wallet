package com.expensetracker.wallet.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expensetracker.wallet.domain.model.User
import com.expensetracker.wallet.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** `currentUser == null` is guest mode (plan.md §85). */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
) : ViewModel() {

    val currentUser: StateFlow<User?> = userRepository.observeCurrentUser()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _isSigningIn = MutableStateFlow(false)
    val isSigningIn: StateFlow<Boolean> = _isSigningIn.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun signInWithGoogle() = runSignIn { userRepository.signInWithGoogle() }

    fun signInWithAzure() = runSignIn { userRepository.signInWithAzure() }

    fun signInWithEmail(email: String, password: String) =
        runSignIn { userRepository.signInWithEmail(email, password) }

    fun signUpWithEmail(email: String, password: String) =
        runSignIn { userRepository.signUpWithEmail(email, password) }

    fun signOut() {
        viewModelScope.launch {
            userRepository.signOut()
                .onFailure { _errorMessage.value = it.message ?: "Couldn't sign out" }
        }
    }

    fun dismissError() {
        _errorMessage.value = null
    }

    private fun runSignIn(action: suspend () -> Result<Unit>) {
        viewModelScope.launch {
            _isSigningIn.value = true
            action()
                .onFailure { _errorMessage.value = it.message ?: "Sign-in failed" }
            _isSigningIn.value = false
        }
    }
}
