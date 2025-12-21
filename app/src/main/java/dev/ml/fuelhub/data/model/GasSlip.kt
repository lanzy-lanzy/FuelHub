package dev.ml.fuelhub.data.model

import java.time.LocalDateTime

enum class GasSlipStatus {
    PENDING,      // Generated but not dispensed yet
    DISPENSED,    // Fuel has been dispensed
    USED,         // Slip has been used (legacy, kept for compatibility)
    CANCELLED     // Slip was cancelled
}

/**
 * Represents a printable gas slip document.
 * Contains all information needed for fuel dispensing at partner gas stations.
 */
data class GasSlip(
    val id: String,
    val transactionId: String,
    val referenceNumber: String,
    val driverName: String,
    val driverFullName: String? = null,  // Full name of driver, fetched from User model
    val vehicleType: String,
    val vehiclePlateNumber: String,
    val destination: String,
    val tripPurpose: String,
    val passengers: String? = null,
    val fuelType: FuelType,
    val litersToPump: Double,
    val transactionDate: LocalDateTime,
    val mdrrmoOfficeId: String,
    val mdrrmoOfficeName: String,
    val generatedAt: LocalDateTime,
    val status: GasSlipStatus = GasSlipStatus.PENDING,
    val dispensedAt: LocalDateTime? = null,
    val dispensedLiters: Double? = null,
    
    // Legacy fields for compatibility
    val isUsed: Boolean = false,
    val usedAt: LocalDateTime? = null
) {
    fun getStatusBadge(): String = when (status) {
        GasSlipStatus.PENDING -> "PENDING"
        GasSlipStatus.DISPENSED -> "DISPENSED"
        GasSlipStatus.USED -> "USED"
        GasSlipStatus.CANCELLED -> "CANCELLED"
    }
}
