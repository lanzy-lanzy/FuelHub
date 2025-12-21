package dev.ml.fuelhub.data.repository

import dev.ml.fuelhub.data.firebase.FirebaseDataSource
import dev.ml.fuelhub.data.model.Vehicle
import dev.ml.fuelhub.domain.repository.VehicleRepository
import kotlinx.coroutines.flow.first
import timber.log.Timber

/**
 * Firebase-based implementation of VehicleRepository.
 * Manages vehicle operations in Firestore.
 */
class FirebaseVehicleRepositoryImpl : VehicleRepository {
    
    override suspend fun getVehicleById(id: String): Vehicle? {
        return try {
            FirebaseDataSource.getVehicleById(id).getOrNull()
        } catch (e: Exception) {
            Timber.e(e, "Error getting vehicle by ID: $id")
            null
        }
    }
    
    override suspend fun getVehicleByPlateNumber(plateNumber: String): Vehicle? {
        return try {
            val allVehicles = FirebaseDataSource.getAllVehicles().first()
            allVehicles.find { it.plateNumber == plateNumber }
        } catch (e: Exception) {
            Timber.e(e, "Error getting vehicle by plate number: $plateNumber")
            null
        }
    }
    
    override suspend fun createVehicle(vehicle: Vehicle): Vehicle {
        return try {
            FirebaseDataSource.createVehicle(vehicle).getOrThrow()
            vehicle
        } catch (e: Exception) {
            Timber.e(e, "Error creating vehicle")
            throw e
        }
    }
    
    override suspend fun updateVehicle(vehicle: Vehicle): Vehicle {
        return try {
            FirebaseDataSource.updateVehicle(vehicle).getOrThrow()
            vehicle
        } catch (e: Exception) {
            Timber.e(e, "Error updating vehicle")
            throw e
        }
    }
    
    override suspend fun getAllActiveVehicles(): List<Vehicle> {
        return try {
            val allVehicles = FirebaseDataSource.getAllVehicles().first()
            allVehicles.filter { it.isActive }
        } catch (e: Exception) {
            Timber.e(e, "Error getting all active vehicles")
            emptyList()
        }
    }
    
    override suspend fun deactivateVehicle(vehicleId: String): Vehicle? {
        return try {
            val vehicle = getVehicleById(vehicleId) ?: return null
            val inactiveVehicle = vehicle.copy(isActive = false)
            FirebaseDataSource.updateVehicle(inactiveVehicle).getOrThrow()
            inactiveVehicle
        } catch (e: Exception) {
            Timber.e(e, "Error deactivating vehicle: $vehicleId")
            null
        }
    }
}
