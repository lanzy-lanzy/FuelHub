package dev.ml.fuelhub.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.ml.fuelhub.data.model.User
import dev.ml.fuelhub.data.model.UserRole
import dev.ml.fuelhub.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDateTime
import java.util.UUID

sealed class DriverManagementUiState {
    object Idle : DriverManagementUiState()
    object Loading : DriverManagementUiState()
    data class Success(val drivers: List<User>) : DriverManagementUiState()
    data class Error(val message: String) : DriverManagementUiState()
    object DriverSaved : DriverManagementUiState()
    object DriverDeleted : DriverManagementUiState()
}

class DriverManagementViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<DriverManagementUiState>(DriverManagementUiState.Idle)
    val uiState: StateFlow<DriverManagementUiState> = _uiState

    private val _selectedDriver = MutableStateFlow<User?>(null)
    val selectedDriver: StateFlow<User?> = _selectedDriver

    init {
        loadDrivers()
    }

    fun loadDrivers() {
        viewModelScope.launch {
            _uiState.value = DriverManagementUiState.Loading
            try {
                val drivers = userRepository.getAllActiveUsers()
                _uiState.value = DriverManagementUiState.Success(drivers)
                Timber.d("Loaded ${drivers.size} drivers")
            } catch (e: Exception) {
                _uiState.value = DriverManagementUiState.Error("Failed to load drivers: ${e.message}")
                Timber.e(e, "Error loading drivers")
            }
        }
    }

    fun addDriver(
        username: String,
        email: String,
        fullName: String,
        officeId: String
    ) {
        viewModelScope.launch {
            try {
                // Check if username already exists
                val existingUser = userRepository.getUserByUsername(username)
                if (existingUser != null) {
                    _uiState.value = DriverManagementUiState.Error("Username '$username' already exists. Please use a different username.")
                    return@launch
                }
                
                val newDriver = User(
                    id = UUID.randomUUID().toString(),
                    username = username,
                    email = email,
                    fullName = fullName,
                    role = UserRole.DISPATCHER,
                    officeId = officeId,
                    isActive = true,
                    createdAt = LocalDateTime.now()
                )
                userRepository.createUser(newDriver)
                _uiState.value = DriverManagementUiState.DriverSaved
                loadDrivers()
                Timber.d("Driver added: $fullName")
            } catch (e: Exception) {
                val errorMsg = when {
                    e.message?.contains("UNIQUE constraint", ignoreCase = true) == true -> 
                        "Username or email already exists. Please use different values."
                    e.message?.contains("NOT NULL constraint", ignoreCase = true) == true ->
                        "All fields are required. Please fill in all information."
                    else -> "Failed to add driver: ${e.message}"
                }
                _uiState.value = DriverManagementUiState.Error(errorMsg)
                Timber.e(e, "Error adding driver")
            }
        }
    }

    fun updateDriver(driver: User) {
        viewModelScope.launch {
            try {
                userRepository.updateUser(driver)
                _uiState.value = DriverManagementUiState.DriverSaved
                loadDrivers()
                _selectedDriver.value = null
                Timber.d("Driver updated: ${driver.fullName}")
            } catch (e: Exception) {
                _uiState.value = DriverManagementUiState.Error("Failed to update driver: ${e.message}")
                Timber.e(e, "Error updating driver")
            }
        }
    }

    fun deleteDriver(driverId: String) {
        viewModelScope.launch {
            try {
                userRepository.deactivateUser(driverId)
                _uiState.value = DriverManagementUiState.DriverDeleted
                loadDrivers()
                _selectedDriver.value = null
                Timber.d("Driver deleted: $driverId")
            } catch (e: Exception) {
                _uiState.value = DriverManagementUiState.Error("Failed to delete driver: ${e.message}")
                Timber.e(e, "Error deleting driver")
            }
        }
    }

    fun selectDriver(driver: User) {
        _selectedDriver.value = driver
    }

    fun clearSelection() {
        _selectedDriver.value = null
    }

    fun resetState() {
        _uiState.value = DriverManagementUiState.Idle
    }
}
