package dev.ml.fuelhub.data.repository

import dev.ml.fuelhub.data.firebase.FirebaseDataSource
import dev.ml.fuelhub.data.model.FuelWallet
import dev.ml.fuelhub.domain.repository.FuelWalletRepository
import kotlinx.coroutines.flow.first
import timber.log.Timber
import java.time.LocalDateTime

/**
 * Firebase-based implementation of FuelWalletRepository.
 * Manages fuel wallet operations in Firestore with atomic updates.
 */
class FirebaseFuelWalletRepositoryImpl : FuelWalletRepository {
    
    override suspend fun getWalletById(id: String): FuelWallet? {
        return try {
            FirebaseDataSource.getFuelWalletById(id).getOrNull()
        } catch (e: Exception) {
            Timber.e(e, "Error getting wallet by ID: $id")
            null
        }
    }
    
    override suspend fun getWalletByOfficeId(officeId: String): FuelWallet? {
        return try {
            val allWallets = FirebaseDataSource.getAllFuelWallets().first()
            allWallets.find { it.officeId == officeId }
        } catch (e: Exception) {
            Timber.e(e, "Error getting wallet by office ID: $officeId")
            null
        }
    }
    
    override suspend fun createWallet(wallet: FuelWallet): FuelWallet {
        return try {
            FirebaseDataSource.createFuelWallet(wallet).getOrThrow()
            wallet
        } catch (e: Exception) {
            Timber.e(e, "Error creating wallet")
            throw e
        }
    }
    
    override suspend fun updateWallet(wallet: FuelWallet): FuelWallet {
        return try {
            val updatedWallet = wallet.copy(lastUpdated = LocalDateTime.now())
            FirebaseDataSource.updateFuelWallet(updatedWallet).getOrThrow()
            updatedWallet
        } catch (e: Exception) {
            Timber.e(e, "Error updating wallet")
            throw e
        }
    }
    
    override suspend fun getAllWallets(): List<FuelWallet> {
        return try {
            FirebaseDataSource.getAllFuelWallets().first()
        } catch (e: Exception) {
            Timber.e(e, "Error getting all wallets")
            emptyList()
        }
    }
    
    override suspend fun refillWallet(walletId: String, additionalLiters: Double): FuelWallet {
        return try {
            val wallet = getWalletById(walletId) 
                ?: throw IllegalArgumentException("Wallet not found: $walletId")
            
            // Calculate new balance ensuring it doesn't exceed capacity
            val newBalance = minOf(wallet.balanceLiters + additionalLiters, wallet.maxCapacityLiters)
            val refillAmount = newBalance - wallet.balanceLiters
            
            val updatedWallet = wallet.copy(
                balanceLiters = newBalance,
                lastUpdated = LocalDateTime.now()
            )
            
            FirebaseDataSource.updateFuelWallet(updatedWallet).getOrThrow()
            Timber.d("Wallet refilled: $walletId with $refillAmount liters")
            updatedWallet
        } catch (e: Exception) {
            Timber.e(e, "Error refilling wallet: $walletId")
            throw e
        }
    }
}
