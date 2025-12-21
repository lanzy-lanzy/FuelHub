package dev.ml.fuelhub.data.model

import java.time.LocalDateTime

/**
 * Audit log entry for tracking all transactions and wallet modifications.
 * Ensures accountability and transparency.
 */
data class AuditLog(
    val id: String,
    val walletId: String,
    val action: String,                    // e.g., "TRANSACTION_CREATED", "WALLET_DEDUCTED"
    val transactionId: String?,
    val performedBy: String,
    val previousBalance: Double,
    val newBalance: Double,
    val litersDifference: Double,
    val description: String,
    val timestamp: LocalDateTime,
    val ipAddress: String? = null
)
