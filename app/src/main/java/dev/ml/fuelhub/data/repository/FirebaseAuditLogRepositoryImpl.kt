package dev.ml.fuelhub.data.repository

import dev.ml.fuelhub.data.firebase.FirebaseDataSource
import dev.ml.fuelhub.data.model.AuditLog
import dev.ml.fuelhub.domain.repository.AuditLogRepository
import kotlinx.coroutines.flow.first
import timber.log.Timber
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

/**
 * Firebase-based implementation of AuditLogRepository.
 * Manages audit log operations in Firestore for accountability and transparency.
 */
class FirebaseAuditLogRepositoryImpl : AuditLogRepository {
    
    override suspend fun logAction(
        walletId: String,
        action: String,
        transactionId: String?,
        performedBy: String,
        previousBalance: Double,
        newBalance: Double,
        litersDifference: Double,
        description: String,
        ipAddress: String?
    ): AuditLog {
        return try {
            val auditLog = AuditLog(
                id = UUID.randomUUID().toString(),
                walletId = walletId,
                action = action,
                transactionId = transactionId,
                performedBy = performedBy,
                previousBalance = previousBalance,
                newBalance = newBalance,
                litersDifference = litersDifference,
                description = description,
                timestamp = LocalDateTime.now(),
                ipAddress = ipAddress
            )
            
            FirebaseDataSource.createAuditLog(auditLog).getOrThrow()
            Timber.d("Audit log created: $action for wallet $walletId")
            auditLog
        } catch (e: Exception) {
            Timber.e(e, "Error logging action: $action")
            throw e
        }
    }
    
    override suspend fun getAuditLogsByWallet(walletId: String): List<AuditLog> {
        return try {
            val allLogs = FirebaseDataSource.getAllAuditLogs().first()
            allLogs.filter { it.walletId == walletId }
        } catch (e: Exception) {
            Timber.e(e, "Error getting audit logs for wallet: $walletId")
            emptyList()
        }
    }
    
    override suspend fun getAuditLogsByDateRange(startDate: LocalDate, endDate: LocalDate): List<AuditLog> {
        return try {
            // Query Firestore for audit logs within date range
            emptyList()
        } catch (e: Exception) {
            Timber.e(e, "Error getting audit logs for date range: $startDate to $endDate")
            emptyList()
        }
    }
    
    override suspend fun getAuditLogsByAction(action: String): List<AuditLog> {
        return try {
            val allLogs = FirebaseDataSource.getAllAuditLogs().first()
            allLogs.filter { it.action == action }
        } catch (e: Exception) {
            Timber.e(e, "Error getting audit logs for action: $action")
            emptyList()
        }
    }
    
    override suspend fun getAuditLogsByUser(userId: String): List<AuditLog> {
        return try {
            val allLogs = FirebaseDataSource.getAllAuditLogs().first()
            allLogs.filter { it.performedBy == userId }
        } catch (e: Exception) {
            Timber.e(e, "Error getting audit logs for user: $userId")
            emptyList()
        }
    }
}
