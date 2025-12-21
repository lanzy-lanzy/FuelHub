package dev.ml.fuelhub.domain.repository

import dev.ml.fuelhub.data.model.AuditLog
import java.time.LocalDate

/**
 * Repository interface for audit log operations.
 * Ensures accountability and transparency through comprehensive logging.
 */
interface AuditLogRepository {
    
    /**
     * Log an action with balance changes.
     */
    suspend fun logAction(
        walletId: String,
        action: String,
        transactionId: String?,
        performedBy: String,
        previousBalance: Double,
        newBalance: Double,
        litersDifference: Double,
        description: String,
        ipAddress: String? = null
    ): AuditLog
    
    /**
     * Get audit logs for a wallet.
     */
    suspend fun getAuditLogsByWallet(walletId: String): List<AuditLog>
    
    /**
     * Get audit logs by date range.
     */
    suspend fun getAuditLogsByDateRange(startDate: LocalDate, endDate: LocalDate): List<AuditLog>
    
    /**
     * Get audit logs by action type.
     */
    suspend fun getAuditLogsByAction(action: String): List<AuditLog>
    
    /**
     * Get audit logs performed by a user.
     */
    suspend fun getAuditLogsByUser(userId: String): List<AuditLog>
}
