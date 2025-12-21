package dev.ml.fuelhub.data.model

/**
 * Enum representing possible transaction states.
 * Tracks the lifecycle of a fuel transaction.
 */
enum class TransactionStatus {
    PENDING,      // Transaction created but not yet confirmed
    APPROVED,     // Transaction approved by authorized user
    COMPLETED,    // Fuel dispensed, wallet deducted
    CANCELLED,    // Transaction cancelled before completion
    FAILED        // Transaction failed due to validation errors
}
