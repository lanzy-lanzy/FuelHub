package dev.ml.fuelhub.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ml.fuelhub.data.model.FuelTransaction
import dev.ml.fuelhub.data.model.FuelType
import dev.ml.fuelhub.data.model.TransactionStatus
import dev.ml.fuelhub.domain.usecase.GenerateDailyReportUseCase
import dev.ml.fuelhub.domain.usecase.GenerateWeeklyReportUseCase
import dev.ml.fuelhub.domain.usecase.GenerateMonthlyReportUseCase
import dev.ml.fuelhub.domain.repository.FuelTransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import javax.inject.Inject

data class ReportFilterState(
    val dateFrom: LocalDate = LocalDate.now().minusMonths(1),
    val dateTo: LocalDate = LocalDate.now(),
    val selectedFuelTypes: Set<FuelType> = FuelType.values().toSet(),
    val selectedStatuses: Set<TransactionStatus> = TransactionStatus.values().toSet(),
    val selectedVehicles: Set<String> = emptySet(),
    val selectedDrivers: Set<String> = emptySet(),
    val minLiters: Double = 0.0,
    val maxLiters: Double = Double.MAX_VALUE,
    val searchKeyword: String = ""
)

data class ReportFilteredData(
    val transactions: List<FuelTransaction> = emptyList(),
    val totalLiters: Double = 0.0,
    val totalCost: Double = 0.0,
    val transactionCount: Int = 0,
    val averageLitersPerTransaction: Double = 0.0,
    val completedCount: Int = 0,
    val pendingCount: Int = 0,
    val failedCount: Int = 0
)

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val dailyReportUseCase: GenerateDailyReportUseCase,
    private val weeklyReportUseCase: GenerateWeeklyReportUseCase,
    private val monthlyReportUseCase: GenerateMonthlyReportUseCase,
    private val transactionRepository: FuelTransactionRepository
) : ViewModel() {

    private val _filterState = MutableStateFlow(ReportFilterState())
    val filterState: StateFlow<ReportFilterState> = _filterState

    private val _filteredData = MutableStateFlow(ReportFilteredData())
    val filteredData: StateFlow<ReportFilteredData> = _filteredData

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _availableVehicles = MutableStateFlow<List<String>>(emptyList())
    val availableVehicles: StateFlow<List<String>> = _availableVehicles

    private val _availableDrivers = MutableStateFlow<List<String>>(emptyList())
    val availableDrivers: StateFlow<List<String>> = _availableDrivers

    init {
        Timber.d("ReportsViewModel initialized")
        viewModelScope.launch {
            try {
                fetchAvailableFilters()
                applyFilters()
            } catch (e: Exception) {
                Timber.e(e, "Error initializing ReportsViewModel")
                _errorMessage.value = "Error initializing reports: ${e.message}"
            }
        }
    }

    fun updateDateRange(from: LocalDate, to: LocalDate) {
        _filterState.value = _filterState.value.copy(
            dateFrom = from,
            dateTo = to
        )
        applyFilters()
    }

    fun updateFuelTypes(types: Set<FuelType>) {
        _filterState.value = _filterState.value.copy(selectedFuelTypes = types)
        applyFilters()
    }

    fun updateStatuses(statuses: Set<TransactionStatus>) {
        _filterState.value = _filterState.value.copy(selectedStatuses = statuses)
        applyFilters()
    }

    fun updateVehicles(vehicles: Set<String>) {
        _filterState.value = _filterState.value.copy(selectedVehicles = vehicles)
        applyFilters()
    }

    fun updateDrivers(drivers: Set<String>) {
        _filterState.value = _filterState.value.copy(selectedDrivers = drivers)
        applyFilters()
    }

    fun updateLiterRange(min: Double, max: Double) {
        _filterState.value = _filterState.value.copy(
            minLiters = min,
            maxLiters = max
        )
        applyFilters()
    }

    fun updateSearchKeyword(keyword: String) {
        _filterState.value = _filterState.value.copy(searchKeyword = keyword)
        applyFilters()
    }

    fun resetFilters() {
        _filterState.value = ReportFilterState()
        applyFilters()
    }

    fun refreshData() {
        Timber.d("Refreshing reports data from Firebase (server)")
        viewModelScope.launch {
            try {
                _isLoading.value = true
                // Fetch directly from server to bypass cache
                val transactions = transactionRepository.getAllTransactionsFromServer()
                val vehicles = transactions.map { it.vehicleId }.distinct()
                val drivers = transactions.map { it.driverName }.distinct()
                
                _availableVehicles.value = vehicles
                _availableDrivers.value = drivers
                
                // Re-apply current filters
                applyFilters()
                _errorMessage.value = null
                Timber.d("Data refreshed successfully from server - ${transactions.size} transactions")
            } catch (e: Exception) {
                Timber.e(e, "Error refreshing data: ${e.message}")
                _errorMessage.value = "Error refreshing data: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun fetchAvailableFilters() {
        viewModelScope.launch {
            try {
                val transactions = transactionRepository.getAllTransactions()
                val vehicles = transactions.map { it.vehicleId }.distinct()
                val drivers = transactions.map { it.driverName }.distinct()
                
                _availableVehicles.value = vehicles
                _availableDrivers.value = drivers
            } catch (e: Exception) {
                Timber.e(e, "Error fetching available filters")
            }
        }
    }

    private fun applyFilters() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val filters = _filterState.value
                
                // Fetch all transactions in date range
                val allTransactions = transactionRepository.getTransactionsByDateRange(
                    filters.dateFrom,
                    filters.dateTo
                )
                
                // Apply filters
                val filtered = allTransactions.filter { transaction ->
                    (transaction.fuelType in filters.selectedFuelTypes) &&
                    (transaction.status in filters.selectedStatuses) &&
                    (filters.selectedVehicles.isEmpty() || transaction.vehicleId in filters.selectedVehicles) &&
                    (filters.selectedDrivers.isEmpty() || transaction.driverName in filters.selectedDrivers) &&
                    (transaction.litersToPump in filters.minLiters..filters.maxLiters) &&
                    (filters.searchKeyword.isEmpty() || 
                        transaction.referenceNumber.contains(filters.searchKeyword, ignoreCase = true) ||
                        transaction.driverName.contains(filters.searchKeyword, ignoreCase = true) ||
                        transaction.vehicleId.contains(filters.searchKeyword, ignoreCase = true))
                }
                
                // Calculate statistics
                val completed = filtered.filter { it.status == TransactionStatus.COMPLETED }
                val totalLiters = filtered.sumOf { it.litersToPump } // Sum all filtered transactions for total liters
                val totalCost = filtered.sumOf { it.getTotalCost() } // Calculate actual cost based on cost per liter
                val avgLiters = if (filtered.isNotEmpty()) totalLiters / filtered.size else 0.0
                
                _filteredData.value = ReportFilteredData(
                    transactions = filtered,
                    totalLiters = totalLiters,
                    totalCost = totalCost,
                    transactionCount = filtered.size,
                    averageLitersPerTransaction = avgLiters,
                    completedCount = filtered.count { it.status == TransactionStatus.COMPLETED },
                    pendingCount = filtered.count { it.status == TransactionStatus.PENDING },
                    failedCount = filtered.count { it.status in listOf(TransactionStatus.FAILED, TransactionStatus.CANCELLED) }
                )
                
                _errorMessage.value = null
            } catch (e: Exception) {
                Timber.e(e, "Error applying filters: ${e.message}")
                _errorMessage.value = "Error loading report: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun generatePdfReport(filename: String): String? {
        return try {
            val data = _filteredData.value
            val filters = _filterState.value
            
            // This will be implemented in a PdfReportGenerator utility
            filename // Return filename path after generation
        } catch (e: Exception) {
            Timber.e(e, "Error generating PDF: ${e.message}")
            null
        }
    }
}
