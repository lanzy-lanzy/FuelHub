package dev.ml.fuelhub.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.ml.fuelhub.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val userId: String? = null,
    val userRole: String? = null,
    val userFullName: String? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        // Observe auth state changes
        viewModelScope.launch {
            authRepository.observeAuthState().collect { isLoggedIn ->
                _uiState.value = _uiState.value.copy(
                    isLoggedIn = isLoggedIn,
                    userId = if (isLoggedIn) authRepository.getCurrentUserId() else null
                )
                Timber.d("Auth state changed: isLoggedIn=$isLoggedIn")
                
                // Fetch user role if logged in
                if (isLoggedIn) {
                    fetchUserRole()
                }
            }
        }
    }
    
    private fun fetchUserRole() {
        viewModelScope.launch {
            try {
                val userId = authRepository.getCurrentUserId()
                if (userId != null) {
                    val userRole = authRepository.getUserRole(userId)
                    val userFullName = authRepository.getUserFullName(userId)
                    _uiState.value = _uiState.value.copy(
                        userRole = userRole,
                        userFullName = userFullName
                    )
                    Timber.d("Fetched user role: $userRole")
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to fetch user role")
            }
        }
    }

    fun login(email: String, password: String) {
        if (!validateLoginInputs(email, password)) {
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val result = authRepository.login(email, password)
                result.onSuccess { userId ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoggedIn = true,
                        userId = userId,
                        successMessage = "Login successful!"
                    )
                    Timber.d("Login successful for user: $userId")
                }.onFailure { exception ->
                    val errorMsg = parseFirebaseError(exception.message ?: "Login failed")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = errorMsg
                    )
                    Timber.e(exception, "Login failed")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "An unexpected error occurred"
                )
                Timber.e(e, "Login exception")
            }
        }
    }

    fun register(email: String, password: String, fullName: String, username: String, role: String = "DISPATCHER") {
        if (!validateRegistrationInputs(email, password, fullName, username)) {
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val result = authRepository.register(email, password, fullName, username)
                result.onSuccess { userId ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoggedIn = true,
                        userId = userId,
                        successMessage = "Registration successful!"
                    )
                    Timber.d("Registration successful for user: $userId")
                }.onFailure { exception ->
                    val errorMsg = parseFirebaseError(exception.message ?: "Registration failed")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = errorMsg
                    )
                    Timber.e(exception, "Registration failed")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "An unexpected error occurred"
                )
                Timber.e(e, "Registration exception")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                authRepository.logout().onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoggedIn = false,
                        userId = null,
                        successMessage = "Logged out successfully"
                    )
                    Timber.d("User logged out")
                }.onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Logout failed"
                    )
                    Timber.e(exception, "Logout failed")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "An unexpected error occurred"
                )
                Timber.e(e, "Logout exception")
            }
        }
    }

    fun resetPassword(email: String) {
        if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.value = _uiState.value.copy(error = "Please enter a valid email address")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                authRepository.resetPassword(email).onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Password reset email sent to $email"
                    )
                    Timber.d("Password reset email sent")
                }.onFailure { exception ->
                    val errorMsg = parseFirebaseError(exception.message ?: "Password reset failed")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = errorMsg
                    )
                    Timber.e(exception, "Password reset failed")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "An unexpected error occurred"
                )
                Timber.e(e, "Password reset exception")
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearSuccessMessage() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }

    private fun validateLoginInputs(email: String, password: String): Boolean {
        when {
            email.isBlank() -> {
                _uiState.value = _uiState.value.copy(error = "Please enter your email")
                return false
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                _uiState.value = _uiState.value.copy(error = "Please enter a valid email address")
                return false
            }
            password.isBlank() -> {
                _uiState.value = _uiState.value.copy(error = "Please enter your password")
                return false
            }
            password.length < 6 -> {
                _uiState.value = _uiState.value.copy(error = "Password must be at least 6 characters")
                return false
            }
        }
        return true
    }

    private fun validateRegistrationInputs(
        email: String,
        password: String,
        fullName: String,
        username: String
    ): Boolean {
        when {
            email.isBlank() -> {
                _uiState.value = _uiState.value.copy(error = "Please enter your email")
                return false
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                _uiState.value = _uiState.value.copy(error = "Please enter a valid email address")
                return false
            }
            password.isBlank() -> {
                _uiState.value = _uiState.value.copy(error = "Please enter a password")
                return false
            }
            password.length < 6 -> {
                _uiState.value = _uiState.value.copy(error = "Password must be at least 6 characters")
                return false
            }
            fullName.isBlank() -> {
                _uiState.value = _uiState.value.copy(error = "Please enter your full name")
                return false
            }
            username.isBlank() -> {
                _uiState.value = _uiState.value.copy(error = "Please enter a username")
                return false
            }
            username.length < 3 -> {
                _uiState.value = _uiState.value.copy(error = "Username must be at least 3 characters")
                return false
            }
        }
        return true
    }

    private fun parseFirebaseError(errorMessage: String): String {
        return when {
            errorMessage.contains("EMAIL_EXISTS", ignoreCase = true) ||
            errorMessage.contains("email already in use", ignoreCase = true) -> {
                "This email is already registered. Please login or use a different email."
            }
            errorMessage.contains("INVALID_EMAIL", ignoreCase = true) -> {
                "Invalid email address format."
            }
            errorMessage.contains("WEAK_PASSWORD", ignoreCase = true) -> {
                "Password is too weak. Please use a stronger password."
            }
            errorMessage.contains("user-not-found", ignoreCase = true) -> {
                "No account found with this email address."
            }
            errorMessage.contains("wrong-password", ignoreCase = true) -> {
                "Incorrect password. Please try again."
            }
            errorMessage.contains("too-many-requests", ignoreCase = true) -> {
                "Too many login attempts. Please try again later."
            }
            else -> errorMessage
        }
    }
}
