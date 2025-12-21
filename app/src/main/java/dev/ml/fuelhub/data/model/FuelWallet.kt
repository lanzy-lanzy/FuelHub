package dev.ml.fuelhub.data.model

import java.time.LocalDateTime

/**
 * Represents a fuel wallet with a balance tracked in liters.
 * This is the primary entity for fuel allocation management.
 */
data class FuelWallet(
    val id: String,
    val officeId: String,
    val balanceLiters: Double,
    val maxCapacityLiters: Double,
    val lastUpdated: LocalDateTime,
    val createdAt: LocalDateTime
)
