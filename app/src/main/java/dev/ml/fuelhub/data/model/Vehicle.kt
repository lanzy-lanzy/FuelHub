package dev.ml.fuelhub.data.model

import java.time.LocalDateTime

/**
 * Represents a vehicle in the MDRRMO fleet.
 * Each vehicle has assigned fuel type requirements and can have multiple assigned drivers.
 */
data class Vehicle(
    val id: String,
    val plateNumber: String,
    val vehicleType: String,
    val fuelType: FuelType,
    val driverName: String,  // Legacy: kept for backward compatibility
    val driverId: String? = null,  // Legacy: primary driver's user ID (backward compatibility)
    val assignedDriverIds: List<String> = emptyList(),  // NEW: List of assigned driver IDs (supports multiple drivers)
    val assignedDriverNames: List<String> = emptyList(),  // NEW: List of assigned driver full names
    val isActive: Boolean = true,
    val createdAt: LocalDateTime
)
