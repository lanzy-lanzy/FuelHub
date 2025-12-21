package dev.ml.fuelhub.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.ml.fuelhub.data.model.User
import dev.ml.fuelhub.data.model.Vehicle
import dev.ml.fuelhub.data.model.FuelType
import dev.ml.fuelhub.domain.repository.VehicleRepository
import dev.ml.fuelhub.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDateTime
import java.util.UUID

sealed class VehicleManagementUiState {
    object Idle : VehicleManagementUiState()
    object Loading : VehicleManagementUiState()
    data class Success(val vehicles: List<Vehicle>) : VehicleManagementUiState()
    data class Error(val message: String) : VehicleManagementUiState()
    object VehicleSaved : VehicleManagementUiState()
    object VehicleDeleted : VehicleManagementUiState()
}

class VehicleManagementViewModel(
    private val vehicleRepository: VehicleRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<VehicleManagementUiState>(VehicleManagementUiState.Idle)
    val uiState: StateFlow<VehicleManagementUiState> = _uiState

    private val _selectedVehicle = MutableStateFlow<Vehicle?>(null)
    val selectedVehicle: StateFlow<Vehicle?> = _selectedVehicle
    
    private val _drivers = MutableStateFlow<List<User>>(emptyList())
    val drivers: StateFlow<List<User>> = _drivers

    init {
        loadVehicles()
        loadDrivers()
    }
    
    fun loadDrivers() {
        viewModelScope.launch {
            try {
                val driversList = userRepository.getAllActiveUsers()
                _drivers.value = driversList
                Timber.d("Loaded ${driversList.size} drivers")
            } catch (e: Exception) {
                Timber.e(e, "Error loading drivers")
            }
        }
    }

    fun loadVehicles() {
        viewModelScope.launch {
            _uiState.value = VehicleManagementUiState.Loading
            try {
                val vehicles = vehicleRepository.getAllActiveVehicles()
                _uiState.value = VehicleManagementUiState.Success(vehicles)
                Timber.d("Loaded ${vehicles.size} vehicles")
            } catch (e: Exception) {
                _uiState.value = VehicleManagementUiState.Error("Failed to load vehicles: ${e.message}")
                Timber.e(e, "Error loading vehicles")
            }
        }
    }

    fun addVehicle(
        plateNumber: String,
        vehicleType: String,
        fuelType: FuelType,
        driverName: String,
        driverId: String? = null,
        assignedDriverIds: List<String> = emptyList(),
        assignedDriverNames: List<String> = emptyList()
    ) {
        viewModelScope.launch {
            try {
                // Check if plate number already exists
                val existingVehicle = vehicleRepository.getVehicleByPlateNumber(plateNumber)
                if (existingVehicle != null) {
                    _uiState.value = VehicleManagementUiState.Error("Plate number '$plateNumber' already exists. Please use a different plate number.")
                    return@launch
                }
                
                val newVehicle = Vehicle(
                    id = UUID.randomUUID().toString(),
                    plateNumber = plateNumber,
                    vehicleType = vehicleType,
                    fuelType = fuelType,
                    driverName = driverName,
                    driverId = driverId,  // Legacy field for backward compatibility
                    assignedDriverIds = assignedDriverIds,  // Multiple driver support
                    assignedDriverNames = assignedDriverNames,
                    isActive = true,
                    createdAt = LocalDateTime.now()
                )
                vehicleRepository.createVehicle(newVehicle)
                _uiState.value = VehicleManagementUiState.VehicleSaved
                loadVehicles()
                Timber.d("Vehicle added: $plateNumber with ${assignedDriverIds.size} drivers: ${assignedDriverIds.joinToString(", ")}")
            } catch (e: Exception) {
                val errorMsg = when {
                    e.message?.contains("UNIQUE constraint", ignoreCase = true) == true -> 
                        "Plate number already exists. Please use a different plate number."
                    e.message?.contains("NOT NULL constraint", ignoreCase = true) == true ->
                        "All fields are required. Please fill in all information."
                    else -> "Failed to add vehicle: ${e.message}"
                }
                _uiState.value = VehicleManagementUiState.Error(errorMsg)
                Timber.e(e, "Error adding vehicle")
            }
        }
    }

    fun updateVehicle(vehicle: Vehicle) {
        viewModelScope.launch {
            try {
                vehicleRepository.updateVehicle(vehicle)
                _uiState.value = VehicleManagementUiState.VehicleSaved
                loadVehicles()
                _selectedVehicle.value = null
                Timber.d("Vehicle updated: ${vehicle.plateNumber}")
            } catch (e: Exception) {
                _uiState.value = VehicleManagementUiState.Error("Failed to update vehicle: ${e.message}")
                Timber.e(e, "Error updating vehicle")
            }
        }
    }

    fun deleteVehicle(vehicleId: String) {
        viewModelScope.launch {
            try {
                vehicleRepository.deactivateVehicle(vehicleId)
                _uiState.value = VehicleManagementUiState.VehicleDeleted
                loadVehicles()
                _selectedVehicle.value = null
                Timber.d("Vehicle deleted: $vehicleId")
            } catch (e: Exception) {
                _uiState.value = VehicleManagementUiState.Error("Failed to delete vehicle: ${e.message}")
                Timber.e(e, "Error deleting vehicle")
            }
        }
    }

    fun selectVehicle(vehicle: Vehicle) {
        _selectedVehicle.value = vehicle
    }

    fun clearSelection() {
        _selectedVehicle.value = null
    }

    fun resetState() {
        _uiState.value = VehicleManagementUiState.Idle
    }
}
