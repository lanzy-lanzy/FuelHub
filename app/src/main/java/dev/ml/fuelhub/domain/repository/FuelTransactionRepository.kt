package dev.ml.fuelhub.domain.repository

import dev.ml.fuelhub.data.model.FuelTransaction
import dev.ml.fuelhub.data.model.TransactionStatus
import java.time.LocalDate

/**
 * Repository interface for fuel transaction operations.
 */
interface FuelTransactionRepository {
    
    /**
     * Get transaction by ID.
     */
    suspend fun getTransactionById(id: String): FuelTransaction?
    
    /**
     * Get transaction by reference number.
     */
    suspend fun getTransactionByReference(referenceNumber: String): FuelTransaction?
    
    /**
     * Create a new transaction.
     */
    suspend fun createTransaction(transaction: FuelTransaction): FuelTransaction
    
    /**
     * Update transaction status and details.
     */
    suspend fun updateTransaction(transaction: FuelTransaction): FuelTransaction
    
    /**
     * Get all transactions for a wallet.
     */
    suspend fun getTransactionsByWallet(walletId: String): List<FuelTransaction>
    
    /**
     * Get transactions by status.
     */
    suspend fun getTransactionsByStatus(status: TransactionStatus): List<FuelTransaction>
    
    /**
     * Get daily transactions.
     */
    suspend fun getTransactionsByDate(date: LocalDate): List<FuelTransaction>
    
    /**
     * Get daily transactions for a specific wallet.
     */
    suspend fun getTransactionsByWalletAndDate(walletId: String, date: LocalDate): List<FuelTransaction>
    
    /**
     * Cancel a transaction (if not yet completed).
     */
    suspend fun cancelTransaction(transactionId: String): FuelTransaction?
    
    /**
     * Get all transactions.
     */
    suspend fun getAllTransactions(): List<FuelTransaction>
    
    /**
     * Get transactions within a date range.
     */
    suspend fun getTransactionsByDateRange(from: LocalDate, to: LocalDate): List<FuelTransaction>
    
    /**
     * Force refresh transactions from server (bypass cache).
     */
    suspend fun getAllTransactionsFromServer(): List<FuelTransaction>
}
