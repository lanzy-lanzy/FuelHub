package dev.ml.fuelhub.domain.repository

import dev.ml.fuelhub.data.model.FuelWallet

/**
 * Repository interface for fuel wallet operations.
 * Abstracts data layer for wallet management.
 */
interface FuelWalletRepository {
    
    /**
     * Get a wallet by ID.
     */
    suspend fun getWalletById(id: String): FuelWallet?
    
    /**
     * Get wallet by office ID.
     */
    suspend fun getWalletByOfficeId(officeId: String): FuelWallet?
    
    /**
     * Create a new wallet.
     */
    suspend fun createWallet(wallet: FuelWallet): FuelWallet
    
    /**
     * Update wallet balance.
     * This operation must be atomic and irreversible once committed.
     */
    suspend fun updateWallet(wallet: FuelWallet): FuelWallet
    
    /**
     * Get all wallets (admin only).
     */
    suspend fun getAllWallets(): List<FuelWallet>
    
    /**
     * Refill wallet balance.
     */
    suspend fun refillWallet(walletId: String, additionalLiters: Double): FuelWallet
}
