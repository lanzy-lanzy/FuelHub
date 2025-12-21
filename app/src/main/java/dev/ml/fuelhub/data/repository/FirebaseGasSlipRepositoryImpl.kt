package dev.ml.fuelhub.data.repository

import dev.ml.fuelhub.data.firebase.FirebaseDataSource
import dev.ml.fuelhub.data.model.GasSlip
import dev.ml.fuelhub.data.model.GasSlipStatus
import dev.ml.fuelhub.data.model.TransactionStatus
import dev.ml.fuelhub.domain.repository.GasSlipRepository
import kotlinx.coroutines.flow.first
import timber.log.Timber
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Firebase-based implementation of GasSlipRepository.
 * Manages gas slip operations in Firestore.
 */
class FirebaseGasSlipRepositoryImpl : GasSlipRepository {
    
    override suspend fun getGasSlipById(id: String): GasSlip? {
        return try {
            FirebaseDataSource.getGasSlipById(id).getOrNull()
        } catch (e: Exception) {
            Timber.e(e, "Error getting gas slip by ID: $id")
            null
        }
    }
    
    override suspend fun getGasSlipByTransactionId(transactionId: String): GasSlip? {
        return try {
            FirebaseDataSource.getGasSlipsByTransaction(transactionId).first().firstOrNull()
        } catch (e: Exception) {
            Timber.e(e, "Error getting gas slip by transaction ID: $transactionId")
            null
        }
    }
    
    override suspend fun getGasSlipByReference(referenceNumber: String): GasSlip? {
        return try {
            val allSlips = FirebaseDataSource.getAllGasSlips().first()
            allSlips.find { it.referenceNumber == referenceNumber }
        } catch (e: Exception) {
            Timber.e(e, "Error getting gas slip by reference: $referenceNumber")
            null
        }
    }
    
    override suspend fun createGasSlip(gasSlip: GasSlip): GasSlip {
        return try {
            FirebaseDataSource.createGasSlip(gasSlip).getOrThrow()
            gasSlip
        } catch (e: Exception) {
            Timber.e(e, "Error creating gas slip")
            throw e
        }
    }
    
    override suspend fun updateGasSlip(gasSlip: GasSlip): GasSlip {
        return try {
            FirebaseDataSource.updateGasSlip(gasSlip).getOrThrow()
            gasSlip
        } catch (e: Exception) {
            Timber.e(e, "Error updating gas slip")
            throw e
        }
    }
    
    override suspend fun markAsUsed(gasSlipId: String): GasSlip? {
        return try {
            val gasSlip = getGasSlipById(gasSlipId) ?: return null
            val usedSlip = gasSlip.copy(
                status = GasSlipStatus.USED,
                isUsed = true,
                usedAt = LocalDateTime.now()
            )
            FirebaseDataSource.updateGasSlip(usedSlip).getOrThrow()
            Timber.d("Gas slip marked as used: $gasSlipId")
            usedSlip
        } catch (e: Exception) {
            Timber.e(e, "Error marking gas slip as used: $gasSlipId")
            null
        }
    }
    
    override suspend fun markAsDispensed(gasSlipId: String): GasSlip? {
        return try {
            val gasSlip = getGasSlipById(gasSlipId) ?: return null
            val dispensedSlip = gasSlip.copy(
                status = GasSlipStatus.DISPENSED,
                dispensedAt = LocalDateTime.now(),
                dispensedLiters = gasSlip.litersToPump // By default, all allocated liters are dispensed
            )
            FirebaseDataSource.updateGasSlip(dispensedSlip).getOrThrow()
            Timber.d("Gas slip marked as dispensed: $gasSlipId")
            
            // Also update the associated FuelTransaction to COMPLETED
            try {
                val transaction = FirebaseDataSource.getTransactionById(gasSlip.transactionId).getOrNull()
                if (transaction != null) {
                    val completedTransaction = transaction.copy(
                        status = TransactionStatus.COMPLETED,
                        completedAt = LocalDateTime.now()
                    )
                    FirebaseDataSource.updateTransaction(completedTransaction).getOrThrow()
                    Timber.d("Associated transaction marked as completed: ${gasSlip.transactionId}")
                }
            } catch (e: Exception) {
                Timber.w(e, "Warning: Failed to update associated transaction for gas slip: $gasSlipId")
                // Don't throw - the gas slip was already updated successfully
            }
            
            dispensedSlip
        } catch (e: Exception) {
            Timber.e(e, "Error marking gas slip as dispensed: $gasSlipId")
            null
        }
    }
    
    override suspend fun getUnusedGasSlips(): List<GasSlip> {
        return try {
            Timber.d("Fetching all gas slips from Firestore")
            val allSlips = FirebaseDataSource.getAllGasSlipsOneTime()
            Timber.d("Successfully fetched ${allSlips.size} gas slips total, ${allSlips.count { !it.isUsed }} unused")
            allSlips.filter { !it.isUsed }
        } catch (e: Exception) {
            Timber.e(e, "Error getting unused gas slips: ${e.message}")
            emptyList()
        }
    }
    
    override suspend fun getGasSlipsByDate(date: LocalDate): List<GasSlip> {
        return try {
            // Query Firestore for gas slips created on the given date
            emptyList()
        } catch (e: Exception) {
            Timber.e(e, "Error getting gas slips for date: $date")
            emptyList()
        }
    }
    
    override suspend fun getGasSlipsByOffice(officeId: String): List<GasSlip> {
        return try {
            // Query Firestore for gas slips from the given office
            emptyList()
        } catch (e: Exception) {
            Timber.e(e, "Error getting gas slips for office: $officeId")
            emptyList()
        }
    }
}
