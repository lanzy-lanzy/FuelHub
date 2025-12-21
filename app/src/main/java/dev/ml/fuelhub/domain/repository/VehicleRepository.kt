package dev.ml.fuelhub.domain.repository

import dev.ml.fuelhub.data.model.Vehicle

/**
 * Repository interface for vehicle operations.
 */
interface VehicleRepository {
    
    /**
     * Get vehicle by ID.
     */
    suspend fun getVehicleById(id: String): Vehicle?
    
    /**
     * Get vehicle by plate number.
     */
    suspend fun getVehicleByPlateNumber(plateNumber: String): Vehicle?
    
    /**
     * Create a new vehicle.
     */
    suspend fun createVehicle(vehicle: Vehicle): Vehicle
    
    /**
     * Update vehicle information.
     */
    suspend fun updateVehicle(vehicle: Vehicle): Vehicle
    
    /**
     * Get all active vehicles.
     */
    suspend fun getAllActiveVehicles(): List<Vehicle>
    
    /**
     * Deactivate a vehicle.
     */
    suspend fun deactivateVehicle(vehicleId: String): Vehicle?
}
