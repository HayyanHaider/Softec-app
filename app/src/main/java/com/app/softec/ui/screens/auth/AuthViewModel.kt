package com.app.softec.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.softec.core.result.Resource
import com.app.softec.data.auth.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun signInWithEmail(email: String, password: String) {
        val cleanEmail = email.trim()
        if (!isValidEmail(cleanEmail)) {
            setError("Enter a valid email address")
            return
        }
        if (password.length < 6) {
            setError("Password must be at least 6 characters")
            return
        }

        launchAuthAction {
            authRepository.signInWithEmail(cleanEmail, password)
        }
    }

    fun signUpWithEmail(email: String, password: String, confirmPassword: String) {
        val cleanEmail = email.trim()
        if (!isValidEmail(cleanEmail)) {
            setError("Enter a valid email address")
            return
        }
        if (password.length < 6) {
            setError("Password must be at least 6 characters")
            return
        }
        if (password != confirmPassword) {
            setError("Passwords do not match")
            return
        }

        launchAuthAction {
            authRepository.signUpWithEmail(cleanEmail, password)
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun launchAuthAction(action: suspend () -> Resource<*>) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            when (val result = action()) {
                is Resource.Success -> {
                    _uiState.value = AuthUiState(isLoading = false)
                }

                is Resource.Error -> {
                    _uiState.value = AuthUiState(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }

                is Resource.Loading -> {
                    _uiState.value = AuthUiState(isLoading = true)
                }
            }
        }
    }

    private fun setError(message: String) {
        _uiState.value = AuthUiState(isLoading = false, errorMessage = message)
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}