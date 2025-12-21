package dev.ml.fuelhub.domain.usecase

import dev.ml.fuelhub.domain.repository.FuelTransactionRepository
import java.time.LocalDate

/**
 * Use case for generating daily fuel consumption reports.
 */
class GenerateDailyReportUseCase(
    private val transactionRepository: FuelTransactionRepository
) {
    
    data class DailyReportOutput(
        val date: LocalDate,
        val totalLitersConsumed: Double,
        val transactionCount: Int,
        val completedTransactions: Int,
        val pendingTransactions: Int,
        val failedTransactions: Int,
        val averageLitersPerTransaction: Double
    )
    
    suspend fun execute(date: LocalDate): DailyReportOutput {
        val transactions = transactionRepository.getTransactionsByDate(date)
        
        val totalLiters = transactions
            .filter { it.status.name == "COMPLETED" }
            .sumOf { it.litersToPump }
        
        val completedCount = transactions.count { it.status.name == "COMPLETED" }
        val pendingCount = transactions.count { it.status.name == "PENDING" }
        val failedCount = transactions.count { it.status.name in listOf("FAILED", "CANCELLED") }
        
        val average = if (completedCount > 0) totalLiters / completedCount else 0.0
        
        return DailyReportOutput(
            date = date,
            totalLitersConsumed = totalLiters,
            transactionCount = transactions.size,
            completedTransactions = completedCount,
            pendingTransactions = pendingCount,
            failedTransactions = failedCount,
            averageLitersPerTransaction = average
        )
    }
}
