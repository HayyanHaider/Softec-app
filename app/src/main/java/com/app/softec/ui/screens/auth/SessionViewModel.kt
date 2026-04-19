package com.app.softec.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.softec.data.auth.AuthRepository
import com.app.softec.data.local.AppDatabase
import com.app.softec.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed interface SessionState {
    data object Checking : SessionState
    data object Authenticated : SessionState
    data object Unauthenticated : SessionState
}

@HiltViewModel
class SessionViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val appDatabase: AppDatabase,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _sessionState = MutableStateFlow<SessionState>(SessionState.Checking)
    val sessionState: StateFlow<SessionState> = _sessionState.asStateFlow()

    private val _currentUserProfile = MutableStateFlow<SessionUserProfile?>(null)
    val currentUserProfile: StateFlow<SessionUserProfile?> = _currentUserProfile.asStateFlow()

    init {
        observeAuthState()
    }

    private fun observeAuthState() {
        viewModelScope.launch {
            authRepository.authState().collect { user ->
                _sessionState.value = if (user != null) {
                    SessionState.Authenticated
                } else {
                    SessionState.Unauthenticated
                }

                _currentUserProfile.value = if (user == null) {
                    null
                } else {
                    SessionUserProfile(
                        username = user.displayName
                            ?.takeIf { it.isNotBlank() }
                            ?: user.email?.substringBefore("@")
                            ?: "User",
                        email = user.email,
                        photoUrl = user.photoUrl?.toString()
                    )
                }
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
            clearLocalStorage()
        }
    }

    private suspend fun clearLocalStorage() {
        withContext(Dispatchers.IO) {
            appDatabase.clearAllTables()
            settingsRepository.clearLocalSettings()
        }
    }
}