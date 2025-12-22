package dev.ml.fuelhub.domain.usecase

import dev.ml.fuelhub.data.model.FuelTransaction
import dev.ml.fuelhub.data.model.TransactionStatus
import dev.ml.fuelhub.domain.exception.InsufficientFuelException
import dev.ml.fuelhub.domain.repository.FuelTransactionRepository
import dev.ml.fuelhub.domain.repository.FuelWalletRepository
import timber.log.Timber
import java.time.LocalDateTime

/**
 * Use case for completing a transaction and deducting fuel from wallet.
 * 
 * Flow:
 * 1. Verify transaction status is DISPENSED
 * 2. Get the associated wallet
 * 3. Check if wallet has sufficient balance
 * 4. Deduct liters from wallet
 * 5. Update transaction status to COMPLETED
 * 
 * This operation must be atomic - either both wallet and transaction are updated,
 * or neither is updated.
 */
class CompleteTransactionUseCase(
    private val transactionRepository: FuelTransactionRepository,
    private val walletRepository: FuelWalletRepository
) {
    
    suspend fun execute(transactionId: String): CompleteTransactionOutput {
        try {
            // Get the transaction
            val transaction = transactionRepository.getTransactionById(transactionId)
                ?: throw IllegalArgumentException("Transaction not found: $transactionId")
            
            // Verify transaction is in DISPENSED state
            if (transaction.status != TransactionStatus.DISPENSED) {
                throw IllegalStateException(
                    "Transaction must be in DISPENSED status. Current: ${transaction.status.name}"
                )
            }
            
            // Get the wallet
            val wallet = walletRepository.getWalletById(transaction.walletId)
                ?: throw IllegalArgumentException("Wallet not found: ${transaction.walletId}")
            
            // Check sufficient fuel
            if (wallet.balanceLiters < transaction.litersToPump) {
                throw InsufficientFuelException(
                    "Insufficient fuel. Required: ${transaction.litersToPump} L, Available: ${wallet.balanceLiters} L"
                )
            }
            
            // Deduct fuel from wallet
            val newBalance = wallet.balanceLiters - transaction.litersToPump
            val updatedWallet = wallet.copy(balanceLiters = newBalance)
            walletRepository.updateWallet(updatedWallet)
            
            Timber.d(
                "Wallet ${wallet.id} deducted: ${transaction.litersToPump} L " +
                "(${wallet.balanceLiters} L -> $newBalance L)"
            )
            
            // Update transaction to COMPLETED
            val completedTransaction = transaction.copy(
                status = TransactionStatus.COMPLETED,
                completedAt = LocalDateTime.now()
            )
            transactionRepository.updateTransaction(completedTransaction)
            
            Timber.d("Transaction $transactionId marked as COMPLETED")
            
            return CompleteTransactionOutput(
                transaction = completedTransaction,
                newWalletBalance = newBalance
            )
        } catch (e: Exception) {
            Timber.e(e, "Error completing transaction $transactionId")
            throw e
        }
    }
    
    data class CompleteTransactionOutput(
        val transaction: FuelTransaction,
        val newWalletBalance: Double
    )
}
