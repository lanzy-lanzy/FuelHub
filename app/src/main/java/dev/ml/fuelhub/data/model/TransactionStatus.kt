package dev.ml.fuelhub.data.model

/**
 * Enum representing possible transaction states.
 * Tracks the lifecycle of a fuel transaction.
 */
enum class TransactionStatus {
    PENDING,      // Transaction created but not yet confirmed
    APPROVED,     // Transaction approved by authorized user
    DISPENSED,    // Fuel dispensed at gas station (verified via QR code scan)
    COMPLETED,    // Transaction completed and wallet deducted
    CANCELLED,    // Transaction cancelled before completion
    FAILED        // Transaction failed due to validation errors
}
