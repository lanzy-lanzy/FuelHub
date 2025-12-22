package dev.ml.fuelhub.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ml.fuelhub.data.model.User
import dev.ml.fuelhub.data.model.UserRole
import dev.ml.fuelhub.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDateTime
import javax.inject.Inject

/**
 * ViewModel for Admin Dashboard operations
 * Manages user creation, editing, deactivation, and role management
 */
@HiltViewModel
class AdminViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {
    
    private val _allUsers = MutableStateFlow<List<User>>(emptyList())
    val allUsers: StateFlow<List<User>> = _allUsers
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    private val _errorMessage = MutableStateFlow("")
    val errorMessage: StateFlow<String> = _errorMessage
    
    fun loadAllUsers() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val users = userRepository.getAllUsers()
                _allUsers.value = users.sortedBy { it.fullName }
                Timber.d("Loaded ${users.size} users")
            } catch (e: Exception) {
                Timber.e(e, "Error loading users")
                _errorMessage.value = "Failed to load users: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun createUser(
        username: String,
        email: String,
        fullName: String,
        role: UserRole
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Validate input
                if (username.isBlank() || email.isBlank() || fullName.isBlank()) {
                    _errorMessage.value = "All fields are required"
                    _isLoading.value = false
                    return@launch
                }
                
                val newUser = User(
                    id = "", // Will be set by repository
                    username = username,
                    email = email,
                    fullName = fullName,
                    role = role,
                    officeId = "default", // Can be enhanced later
                    isActive = true,
                    createdAt = LocalDateTime.now()
                )
                
                userRepository.createUser(newUser)
                _errorMessage.value = "User created successfully"
                loadAllUsers()
                Timber.d("User created: $username")
            } catch (e: Exception) {
                Timber.e(e, "Error creating user")
                _errorMessage.value = "Failed to create user: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun updateUser(
        userId: String,
        fullName: String,
        role: UserRole
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val user = _allUsers.value.find { it.id == userId }
                if (user != null) {
                    val updatedUser = user.copy(
                        fullName = fullName,
                        role = role
                    )
                    userRepository.updateUser(updatedUser)
                    _errorMessage.value = "User updated successfully"
                    loadAllUsers()
                    Timber.d("User updated: $userId")
                }
            } catch (e: Exception) {
                Timber.e(e, "Error updating user")
                _errorMessage.value = "Failed to update user: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun changeUserRole(userId: String, newRole: UserRole) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val user = _allUsers.value.find { it.id == userId }
                if (user != null) {
                    val updatedUser = user.copy(role = newRole)
                    userRepository.updateUser(updatedUser)
                    _errorMessage.value = "Role changed to ${newRole.name}"
                    loadAllUsers()
                    Timber.d("User role changed: $userId -> ${newRole.name}")
                }
            } catch (e: Exception) {
                Timber.e(e, "Error changing user role")
                _errorMessage.value = "Failed to change role: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun deactivateUser(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val user = _allUsers.value.find { it.id == userId }
                if (user != null) {
                    val updatedUser = user.copy(isActive = false)
                    userRepository.updateUser(updatedUser)
                    _errorMessage.value = "User deactivated"
                    loadAllUsers()
                    Timber.d("User deactivated: $userId")
                }
            } catch (e: Exception) {
                Timber.e(e, "Error deactivating user")
                _errorMessage.value = "Failed to deactivate user: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun reactivateUser(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val user = _allUsers.value.find { it.id == userId }
                if (user != null) {
                    val updatedUser = user.copy(isActive = true)
                    userRepository.updateUser(updatedUser)
                    _errorMessage.value = "User reactivated"
                    loadAllUsers()
                    Timber.d("User reactivated: $userId")
                }
            } catch (e: Exception) {
                Timber.e(e, "Error reactivating user")
                _errorMessage.value = "Failed to reactivate user: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun clearErrorMessage() {
        _errorMessage.value = ""
    }
}
