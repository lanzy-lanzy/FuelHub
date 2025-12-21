package dev.ml.fuelhub.data.repository

import dev.ml.fuelhub.data.firebase.FirebaseDataSource
import dev.ml.fuelhub.data.model.FuelTransaction
import dev.ml.fuelhub.data.model.TransactionStatus
import dev.ml.fuelhub.domain.repository.FuelTransactionRepository
import kotlinx.coroutines.flow.first
import timber.log.Timber
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Firebase-based implementation of FuelTransactionRepository.
 * Manages fuel transaction operations in Firestore.
 */
class FirebaseFuelTransactionRepositoryImpl : FuelTransactionRepository {
    
    override suspend fun getTransactionById(id: String): FuelTransaction? {
        return try {
            FirebaseDataSource.getTransactionById(id).getOrNull()
        } catch (e: Exception) {
            Timber.e(e, "Error getting transaction by ID: $id")
            null
        }
    }
    
    override suspend fun getTransactionByReference(referenceNumber: String): FuelTransaction? {
        return try {
            val allTransactions = FirebaseDataSource.getAllTransactions().first()
            allTransactions.find { it.referenceNumber == referenceNumber }
        } catch (e: Exception) {
            Timber.e(e, "Error getting transaction by reference: $referenceNumber")
            null
        }
    }
    
    override suspend fun createTransaction(transaction: FuelTransaction): FuelTransaction {
        return try {
            FirebaseDataSource.createTransaction(transaction).getOrThrow()
            transaction
        } catch (e: Exception) {
            Timber.e(e, "Error creating transaction")
            throw e
        }
    }
    
    override suspend fun updateTransaction(transaction: FuelTransaction): FuelTransaction {
        return try {
            FirebaseDataSource.updateTransaction(transaction).getOrThrow()
            transaction
        } catch (e: Exception) {
            Timber.e(e, "Error updating transaction")
            throw e
        }
    }
    
    override suspend fun getTransactionsByWallet(walletId: String): List<FuelTransaction> {
        return try {
            FirebaseDataSource.getTransactionsByWallet(walletId).first()
        } catch (e: Exception) {
            Timber.e(e, "Error getting transactions for wallet: $walletId")
            emptyList()
        }
    }
    
    override suspend fun getTransactionsByStatus(status: TransactionStatus): List<FuelTransaction> {
        return try {
            FirebaseDataSource.getTransactionsByStatus(status).first()
        } catch (e: Exception) {
            Timber.e(e, "Error getting transactions by status: ${status.name}")
            emptyList()
        }
    }
    
    override suspend fun getTransactionsByDate(date: LocalDate): List<FuelTransaction> {
        return try {
            // Get all transactions and filter by date
            val allTransactions = FirebaseDataSource.getAllTransactions().first()
            Timber.d("Total transactions from Firebase: ${allTransactions.size}")
            
            val dateStart = date.atStartOfDay()
            val dateEnd = date.plusDays(1).atStartOfDay()
            
            val filtered = allTransactions.filter { transaction ->
                val createdAt = transaction.createdAt
                createdAt.isAfter(dateStart) && createdAt.isBefore(dateEnd)
            }
            Timber.d("Transactions filtered for $date: ${filtered.size}")
            filtered
        } catch (e: Exception) {
            Timber.e(e, "Error getting transactions for date: $date - ${e.message}")
            emptyList()
        }
    }
    
    override suspend fun getTransactionsByWalletAndDate(
        walletId: String,
        date: LocalDate
    ): List<FuelTransaction> {
        return try {
            // Get all transactions for wallet and filter by date
            val walletTransactions = FirebaseDataSource.getTransactionsByWallet(walletId).first()
            val dateStart = date.atStartOfDay()
            val dateEnd = date.plusDays(1).atStartOfDay()
            
            walletTransactions.filter { transaction ->
                val createdAt = transaction.createdAt
                createdAt.isAfter(dateStart) && createdAt.isBefore(dateEnd)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting transactions for wallet $walletId on date $date")
            emptyList()
        }
    }
    
    override suspend fun cancelTransaction(transactionId: String): FuelTransaction? {
        return try {
            val transaction = getTransactionById(transactionId) ?: return null
            
            // Only allow cancellation for pending transactions
            if (transaction.status != TransactionStatus.PENDING) {
                Timber.w("Cannot cancel transaction with status: ${transaction.status}")
                return null
            }
            
            val cancelledTransaction = transaction.copy(
                status = TransactionStatus.CANCELLED,
                completedAt = LocalDateTime.now()
            )
            
            FirebaseDataSource.updateTransaction(cancelledTransaction).getOrThrow()
            Timber.d("Transaction cancelled: $transactionId")
            cancelledTransaction
        } catch (e: Exception) {
            Timber.e(e, "Error cancelling transaction: $transactionId")
            null
        }
    }
    
    override suspend fun getAllTransactions(): List<FuelTransaction> {
        return try {
            FirebaseDataSource.getAllTransactions().first()
        } catch (e: Exception) {
            Timber.e(e, "Error getting all transactions: ${e.message}")
            emptyList()
        }
    }
    
    override suspend fun getTransactionsByDateRange(from: LocalDate, to: LocalDate): List<FuelTransaction> {
        return try {
            val allTransactions = FirebaseDataSource.getAllTransactions().first()
            
            val dateStart = from.atStartOfDay()
            val dateEnd = to.plusDays(1).atStartOfDay()
            
            val filtered = allTransactions.filter { transaction ->
                val createdAt = transaction.createdAt
                createdAt.isAfter(dateStart) && createdAt.isBefore(dateEnd)
            }
            
            Timber.d("Transactions filtered for $from to $to: ${filtered.size}")
            filtered
        } catch (e: Exception) {
            Timber.e(e, "Error getting transactions for date range: $from to $to - ${e.message}")
            emptyList()
        }
    }
    
    override suspend fun getAllTransactionsFromServer(): List<FuelTransaction> {
        return try {
            FirebaseDataSource.getAllTransactionsFromServer().getOrNull() ?: emptyList()
        } catch (e: Exception) {
            Timber.e(e, "Error getting all transactions from server: ${e.message}")
            emptyList()
        }
    }
}
