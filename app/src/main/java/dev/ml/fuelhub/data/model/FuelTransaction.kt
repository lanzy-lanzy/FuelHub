package dev.ml.fuelhub.data.model

import java.time.LocalDateTime

/**
 * Represents a fuel transaction/gas slip.
 * Core entity for tracking fuel consumption and generating gas slips.
 */
data class FuelTransaction(
    val id: String,
    val referenceNumber: String,           // Unique traceable reference
    val walletId: String,                  // Associated fuel wallet
    val vehicleId: String,
    val driverName: String,                // Driver username/ID
    val driverFullName: String? = null,    // Driver full name for display
    val vehicleType: String,
    val fuelType: FuelType,
    val litersToPump: Double,
    val costPerLiter: Double = 0.0,        // Cost per liter (e.g., 65.50)
    val destination: String,
    val tripPurpose: String,
    val passengers: String? = null,        // Optional: comma-separated names
    val status: TransactionStatus = TransactionStatus.PENDING,
    val createdBy: String,                 // User who created the transaction
    val approvedBy: String? = null,        // User who approved (if applicable)
    val createdAt: LocalDateTime,
    val completedAt: LocalDateTime? = null,
    val notes: String? = null
) {
    /**
     * Calculate total cost based on liters dispensed and cost per liter.
     */
    fun getTotalCost(): Double = litersToPump * costPerLiter
}
