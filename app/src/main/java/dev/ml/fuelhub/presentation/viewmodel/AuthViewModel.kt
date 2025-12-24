package dev.ml.fuelhub.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.ml.fuelhub.domain.repository.AuthRepository
import dev.ml.fuelhub.domain.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import android.net.Uri
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.google.firebase.messaging.FirebaseMessaging
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val userId: String? = null,
    val userRole: String? = null,
    val userFullName: String? = null,
    val userEmail: String? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val notificationRepository: NotificationRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()
    
    private val _profilePictureUrl = MutableStateFlow<String?>(null)
    val profilePictureUrl: StateFlow<String?> = _profilePictureUrl.asStateFlow()

    init {
        // Observe auth state changes
        viewModelScope.launch {
            authRepository.observeAuthState().collect { isLoggedIn ->
                _uiState.value = _uiState.value.copy(
                    isLoggedIn = isLoggedIn,
                    userId = if (isLoggedIn) authRepository.getCurrentUserId() else null
                )
                Timber.d("Auth state changed: isLoggedIn=$isLoggedIn")
                
                // Fetch user role and profile picture if logged in
                if (isLoggedIn) {
                    fetchUserRole()
                    val userId = authRepository.getCurrentUserId()
                    if (userId != null) {
                        loadProfilePictureUrl(userId)
                    }
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
                    
                    // Load profile picture from Firestore
                    val savedPath = authRepository.getUserProfilePictureUrl(userId)
                    var profilePictureUrl: String? = null
                    
                    if (savedPath != null && savedPath.isNotEmpty()) {
                        val profileFile = File(savedPath)
                        if (profileFile.exists()) {
                            profilePictureUrl = "file://$savedPath"
                        }
                    }
                    
                    _uiState.value = _uiState.value.copy(
                        userRole = userRole,
                        userFullName = userFullName,
                        userEmail = authRepository.getCurrentUserEmail()
                    )
                    _profilePictureUrl.value = profilePictureUrl
                    Timber.d("Fetched user role: $userRole, Profile picture: ${profilePictureUrl?.take(50)}...")
                    
                    // Store FCM token for push notifications
                    storeFcmToken(userId)
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to fetch user role")
            }
        }
    }
    
    /**
     * Store FCM token for push notifications
     * Called after user successfully logs in
     */
    private fun storeFcmToken(userId: String) {
        Timber.d("📱 Storing FCM token for user: $userId")
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                Timber.d("📱 FCM Token received: ${token.take(20)}...")
                
                // Store in Firestore
                viewModelScope.launch {
                    try {
                        val success = notificationRepository.storeUserFcmToken(userId, token)
                        if (success) {
                            Timber.d("✓ FCM token stored successfully in Firestore")
                        } else {
                            Timber.w("✗ Failed to store FCM token")
                        }
                    } catch (e: Exception) {
                        Timber.e(e, "❌ Error storing FCM token")
                    }
                }
            } else {
                Timber.e(task.exception, "❌ Failed to get FCM token")
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
    
    fun updateProfilePictureUrl(url: String) {
        _profilePictureUrl.value = url
        Timber.d("Profile picture URL updated: $url")
    }
    
    fun refreshProfilePicture() {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: return@launch
            try {
                val savedPath = authRepository.getUserProfilePictureUrl(userId)
                if (savedPath != null && savedPath.isNotEmpty()) {
                    val profileFile = File(savedPath)
                    if (profileFile.exists()) {
                        val fileUri = "file://$savedPath"
                        _profilePictureUrl.value = fileUri
                        Timber.d("Refreshed profile picture: $fileUri")
                    } else {
                        _profilePictureUrl.value = null
                    }
                } else {
                    _profilePictureUrl.value = null
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to refresh profile picture")
            }
        }
    }
    
    fun loadProfilePictureUrl(userId: String) {
        viewModelScope.launch {
            try {
                // Try to load from Firestore first
                val savedPath = authRepository.getUserProfilePictureUrl(userId)
                
                if (savedPath != null && savedPath.isNotEmpty()) {
                    // Check if the file exists on device
                    val profileFile = File(savedPath)
                    if (profileFile.exists()) {
                        val fileUri = "file://$savedPath"
                        _profilePictureUrl.value = fileUri
                        Timber.d("Loaded profile picture from local storage: $fileUri")
                    } else {
                        Timber.d("Profile picture path in Firestore but file not found: $savedPath")
                        _profilePictureUrl.value = null
                    }
                } else {
                    Timber.d("No profile picture URL stored for user: $userId")
                    _profilePictureUrl.value = null
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to load profile picture URL")
                _profilePictureUrl.value = null
            }
        }
    }
    
    fun uploadProfilePicture(imageUri: Uri) {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: run {
                Timber.e("User not authenticated")
                return@launch
            }
            
            try {
                // Save image to local device storage
                val profileImagePath = saveProfilePictureLocally(userId, imageUri)
                
                if (profileImagePath != null) {
                    // Convert to proper file:// URI
                    val fileUri = "file://$profileImagePath"
                    
                    // Save path to Firestore
                    authRepository.saveProfilePictureUrl(userId, profileImagePath).onSuccess {
                        _profilePictureUrl.value = fileUri
                        Timber.d("Profile picture saved locally and to Firestore: $fileUri")
                    }.onFailure { error ->
                        Timber.e(error, "Failed to save profile picture URL to Firestore")
                        // Still update UI even if Firestore save fails
                        _profilePictureUrl.value = fileUri
                    }
                } else {
                    Timber.e("Failed to save profile picture locally")
                }
            } catch (e: Exception) {
                Timber.e(e, "Error uploading profile picture: ${e.message}")
            }
        }
    }
    
    /**
     * Save profile picture to app's private files directory
     * Returns the file path if successful, null otherwise
     */
    private fun saveProfilePictureLocally(userId: String, imageUri: Uri): String? {
        return try {
            // Create profiles directory in app's private files directory
            val profilesDir = File(context.filesDir, "profiles")
            if (!profilesDir.exists()) {
                profilesDir.mkdirs()
            }
            
            // Create file with user ID as filename
            val profileFile = File(profilesDir, "$userId.jpg")
            
            // Read bitmap from URI and save it
            val bitmap = BitmapFactory.decodeStream(context.contentResolver.openInputStream(imageUri))
            
            if (bitmap != null) {
                FileOutputStream(profileFile).use { fos ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fos)
                }
                Timber.d("Profile picture saved to: ${profileFile.absolutePath}")
                profileFile.absolutePath
            } else {
                Timber.e("Failed to decode image bitmap")
                null
            }
        } catch (e: Exception) {
            Timber.e(e, "Error saving profile picture locally: ${e.message}")
            null
        }
    }
}
