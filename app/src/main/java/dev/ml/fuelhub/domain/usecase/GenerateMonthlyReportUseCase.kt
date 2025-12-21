package dev.ml.fuelhub.domain.usecase

import dev.ml.fuelhub.domain.repository.FuelTransactionRepository
import java.time.LocalDate
import java.time.YearMonth

/**
 * Use case for generating monthly fuel consumption reports.
 */
class GenerateMonthlyReportUseCase(
    private val transactionRepository: FuelTransactionRepository
) {
    
    data class MonthlyReportOutput(
        val yearMonth: YearMonth,
        val totalLitersConsumed: Double,
        val totalTransactions: Int,
        val completedTransactions: Int,
        val pendingTransactions: Int,
        val cancelledTransactions: Int,
        val averageDailyConsumption: Double,
        val weeklyBreakdown: Map<String, Double>
    )
    
    suspend fun execute(yearMonth: YearMonth): MonthlyReportOutput {
        val startDate = yearMonth.atDay(1)
        val endDate = yearMonth.atEndOfMonth()
        
        val weeklyBreakdown = mutableMapOf<String, Double>()
        var currentDate = startDate
        var totalLiters = 0.0
        var totalCompleted = 0
        var totalPending = 0
        var totalCancelled = 0
        
        while (currentDate <= endDate) {
            val dayTransactions = transactionRepository.getTransactionsByDate(currentDate)
            val dayLiters = dayTransactions
                .filter { it.status.name == "COMPLETED" }
                .sumOf { it.litersToPump }
            
            totalLiters += dayLiters
            totalCompleted += dayTransactions.count { it.status.name == "COMPLETED" }
            totalPending += dayTransactions.count { it.status.name == "PENDING" }
            totalCancelled += dayTransactions.count { it.status.name in listOf("CANCELLED", "FAILED") }
            
            // Weekly aggregation (every 7 days)
            if (currentDate.dayOfMonth % 7 == 0 || currentDate == endDate) {
                val weekLabel = "Week ${(currentDate.dayOfMonth - 1) / 7 + 1}"
                weeklyBreakdown[weekLabel] = dayLiters
            }
            
            currentDate = currentDate.plusDays(1)
        }
        
        val dayCount = endDate.dayOfMonth
        val averageDailyConsumption = if (dayCount > 0) totalLiters / dayCount else 0.0
        
        return MonthlyReportOutput(
            yearMonth = yearMonth,
            totalLitersConsumed = totalLiters,
            totalTransactions = totalCompleted + totalPending + totalCancelled,
            completedTransactions = totalCompleted,
            pendingTransactions = totalPending,
            cancelledTransactions = totalCancelled,
            averageDailyConsumption = averageDailyConsumption,
            weeklyBreakdown = weeklyBreakdown
        )
    }
}
