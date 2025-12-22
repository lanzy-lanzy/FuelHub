package dev.ml.fuelhub.domain.repository

import dev.ml.fuelhub.data.model.GasSlip
import java.time.LocalDate

/**
 * Repository interface for gas slip operations.
 * Manages the printable/dispensable fuel slips.
 */
interface GasSlipRepository {
    
    /**
     * Get gas slip by ID.
     */
    suspend fun getGasSlipById(id: String): GasSlip?
    
    /**
     * Get gas slip by transaction ID.
     */
    suspend fun getGasSlipByTransactionId(transactionId: String): GasSlip?
    
    /**
     * Get gas slip by reference number.
     */
    suspend fun getGasSlipByReference(referenceNumber: String): GasSlip?
    
    /**
     * Create a new gas slip.
     */
    suspend fun createGasSlip(gasSlip: GasSlip): GasSlip
    
    /**
     * Update gas slip (e.g., mark as used).
     */
    suspend fun updateGasSlip(gasSlip: GasSlip): GasSlip
    
    /**
     * Mark gas slip as used.
     */
    suspend fun markAsUsed(gasSlipId: String): GasSlip?
    
    /**
     * Mark gas slip as dispensed.
     */
    suspend fun markAsDispensed(gasSlipId: String): GasSlip?
    
    /**
     * Get all unused gas slips.
     */
    suspend fun getUnusedGasSlips(): List<GasSlip>
    
    /**
     * Get all gas slips (used, unused, dispensed, etc.).
     * Used for list views that need to show complete transaction history.
     */
    suspend fun getAllGasSlips(): List<GasSlip>
    
    /**
     * Get gas slips by date.
     */
    suspend fun getGasSlipsByDate(date: LocalDate): List<GasSlip>
    
    /**
     * Get gas slips by office.
     */
    suspend fun getGasSlipsByOffice(officeId: String): List<GasSlip>
    
    /**
     * Cancel a gas slip.
     */
    suspend fun cancelGasSlip(gasSlipId: String): GasSlip?
    
    /**
     * Bulk mark pending gas slips as dispensed.
     */
    suspend fun bulkMarkPendingAsDispensed(gasSlipIds: List<String>): Boolean
    }
