package dev.ml.fuelhub.domain.usecase

import dev.ml.fuelhub.domain.repository.FuelTransactionRepository
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Use case for generating weekly fuel consumption reports.
 */
class GenerateWeeklyReportUseCase(
    private val transactionRepository: FuelTransactionRepository
) {
    
    data class WeeklyReportOutput(
        val startDate: LocalDate,
        val endDate: LocalDate,
        val totalLitersConsumed: Double,
        val totalTransactions: Int,
        val completedTransactions: Int,
        val pendingTransactions: Int,
        val averageDailyConsumption: Double,
        val dailyBreakdown: Map<LocalDate, Double>
    )
    
    suspend fun execute(startDate: LocalDate, endDate: LocalDate = startDate.plusDays(6)): WeeklyReportOutput {
        val dailyBreakdown = mutableMapOf<LocalDate, Double>()
        var currentDate = startDate
        var totalLiters = 0.0
        var totalCompleted = 0
        var totalPending = 0
        
        while (currentDate <= endDate) {
            val dayTransactions = transactionRepository.getTransactionsByDate(currentDate)
            val dayLiters = dayTransactions
                .filter { it.status.name == "COMPLETED" }
                .sumOf { it.litersToPump }
            
            dailyBreakdown[currentDate] = dayLiters
            totalLiters += dayLiters
            totalCompleted += dayTransactions.count { it.status.name == "COMPLETED" }
            totalPending += dayTransactions.count { it.status.name == "PENDING" }
            
            currentDate = currentDate.plusDays(1)
        }
        
        val dayCount = ChronoUnit.DAYS.between(startDate, endDate) + 1
        val averageDailyConsumption = totalLiters / dayCount
        
        return WeeklyReportOutput(
            startDate = startDate,
            endDate = endDate,
            totalLitersConsumed = totalLiters,
            totalTransactions = totalCompleted + totalPending,
            completedTransactions = totalCompleted,
            pendingTransactions = totalPending,
            averageDailyConsumption = averageDailyConsumption,
            dailyBreakdown = dailyBreakdown
        )
    }
}
