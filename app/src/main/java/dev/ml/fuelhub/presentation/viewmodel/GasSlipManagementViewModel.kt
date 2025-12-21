package dev.ml.fuelhub.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.ml.fuelhub.data.model.GasSlip
import dev.ml.fuelhub.domain.repository.GasSlipRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate
import java.time.LocalDateTime

sealed class GasSlipUiState {
    object Idle : GasSlipUiState()
    object Loading : GasSlipUiState()
    data class Success(val gasSlips: List<GasSlip>) : GasSlipUiState()
    data class Error(val message: String) : GasSlipUiState()
    object PrintSuccess : GasSlipUiState()
    object ExportSuccess : GasSlipUiState()
}

class GasSlipManagementViewModel(
    private val gasSlipRepository: GasSlipRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<GasSlipUiState>(GasSlipUiState.Idle)
    val uiState: StateFlow<GasSlipUiState> = _uiState
    
    private val _selectedGasSlip = MutableStateFlow<GasSlip?>(null)
    val selectedGasSlip: StateFlow<GasSlip?> = _selectedGasSlip
    
    private val _filterByStatus = MutableStateFlow<String>("ALL") // ALL, PENDING, USED
    val filterByStatus: StateFlow<String> = _filterByStatus
    
    private val _allGasSlips = MutableStateFlow<List<GasSlip>>(emptyList())
    val allGasSlips: StateFlow<List<GasSlip>> = _allGasSlips
    
    // Advanced filtering and search
    private val _searchQuery = MutableStateFlow<String>("")
    val searchQuery: StateFlow<String> = _searchQuery
    
    private val _fuelTypeFilter = MutableStateFlow<String?>(null)
    val fuelTypeFilter: StateFlow<String?> = _fuelTypeFilter
    
    private val _vehicleFilter = MutableStateFlow<String?>(null)
    val vehicleFilter: StateFlow<String?> = _vehicleFilter
    
    private val _driverFilter = MutableStateFlow<String?>(null)
    val driverFilter: StateFlow<String?> = _driverFilter
    
    private val _dateRangeStart = MutableStateFlow<LocalDateTime?>(null)
    val dateRangeStart: StateFlow<LocalDateTime?> = _dateRangeStart
    
    private val _dateRangeEnd = MutableStateFlow<LocalDateTime?>(null)
    val dateRangeEnd: StateFlow<LocalDateTime?> = _dateRangeEnd
    
    private val _sortByLatest = MutableStateFlow<Boolean>(true) // Sort newest first
    val sortByLatest: StateFlow<Boolean> = _sortByLatest
    
    init {
        try {
            loadAllGasSlips()
        } catch (e: Exception) {
            Timber.e(e, "Error initializing GasSlipManagementViewModel")
            _uiState.value = GasSlipUiState.Error("Initialization error: ${e.message}")
        }
    }
    
    fun loadAllGasSlips() {
        viewModelScope.launch {
            _uiState.value = GasSlipUiState.Loading
            try {
                val slips = gasSlipRepository.getUnusedGasSlips()
                _allGasSlips.value = slips
                _uiState.value = GasSlipUiState.Success(slips)
                Timber.d("Loaded ${slips.size} gas slips")
            } catch (e: Exception) {
                _uiState.value = GasSlipUiState.Error("Failed to load gas slips: ${e.message}")
                Timber.e(e, "Error loading gas slips")
            }
        }
    }
    
    fun loadGasSlipsByDate(date: LocalDate) {
        viewModelScope.launch {
            _uiState.value = GasSlipUiState.Loading
            try {
                val slips = gasSlipRepository.getGasSlipsByDate(date)
                _allGasSlips.value = slips
                _uiState.value = GasSlipUiState.Success(slips)
                Timber.d("Loaded ${slips.size} gas slips for date: $date")
            } catch (e: Exception) {
                _uiState.value = GasSlipUiState.Error("Failed to load gas slips: ${e.message}")
                Timber.e(e, "Error loading gas slips by date")
            }
        }
    }
    
    fun loadGasSlipsByOffice(officeId: String) {
        viewModelScope.launch {
            _uiState.value = GasSlipUiState.Loading
            try {
                val slips = gasSlipRepository.getGasSlipsByOffice(officeId)
                _allGasSlips.value = slips
                _uiState.value = GasSlipUiState.Success(slips)
                Timber.d("Loaded ${slips.size} gas slips for office: $officeId")
            } catch (e: Exception) {
                _uiState.value = GasSlipUiState.Error("Failed to load gas slips: ${e.message}")
                Timber.e(e, "Error loading gas slips by office")
            }
        }
    }
    
    fun selectGasSlip(gasSlip: GasSlip) {
        _selectedGasSlip.value = gasSlip
    }
    
    fun clearSelection() {
        _selectedGasSlip.value = null
    }
    
    fun setFilterStatus(status: String) {
        _filterByStatus.value = status
    }
    
    fun getFilteredGasSlips(): List<GasSlip> {
        val allSlips = _allGasSlips.value
        return when (_filterByStatus.value) {
            "PENDING" -> allSlips.filter { !it.isUsed }
            "USED" -> allSlips.filter { it.isUsed }
            else -> allSlips
        }
    }
    
    fun getFilteredAndSearchedGasSlips(): List<GasSlip> {
        var filtered = _allGasSlips.value
        
        // Apply status filter
        filtered = when (_filterByStatus.value) {
            "PENDING" -> filtered.filter { it.status == dev.ml.fuelhub.data.model.GasSlipStatus.PENDING }
            "USED" -> filtered.filter { it.status == dev.ml.fuelhub.data.model.GasSlipStatus.USED || it.status == dev.ml.fuelhub.data.model.GasSlipStatus.DISPENSED }
            else -> filtered
        }
        
        // Apply search query
        val query = _searchQuery.value.lowercase().trim()
        if (query.isNotEmpty()) {
            filtered = filtered.filter { slip ->
                slip.referenceNumber.lowercase().contains(query) ||
                slip.driverName.lowercase().contains(query) ||
                slip.driverFullName?.lowercase()?.contains(query) ?: false ||
                slip.vehiclePlateNumber.lowercase().contains(query) ||
                slip.destination.lowercase().contains(query) ||
                slip.tripPurpose.lowercase().contains(query)
            }
        }
        
        // Apply fuel type filter
        _fuelTypeFilter.value?.let { fuelType ->
            filtered = filtered.filter { it.fuelType.name == fuelType }
        }
        
        // Apply vehicle filter
        _vehicleFilter.value?.let { vehicle ->
            filtered = filtered.filter { it.vehiclePlateNumber == vehicle }
        }
        
        // Apply driver filter
        _driverFilter.value?.let { driver ->
            filtered = filtered.filter { it.driverName == driver || it.driverFullName == driver }
        }
        
        // Apply date range filter
        _dateRangeStart.value?.let { startDate ->
            filtered = filtered.filter { it.transactionDate >= startDate }
        }
        _dateRangeEnd.value?.let { endDate ->
            filtered = filtered.filter { it.transactionDate <= endDate }
        }
        
        // Sort by date (newest first if _sortByLatest is true)
        return if (_sortByLatest.value) {
            filtered.sortedByDescending { it.transactionDate }
        } else {
            filtered.sortedBy { it.transactionDate }
        }
    }
    
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    fun setFuelTypeFilter(fuelType: String?) {
        _fuelTypeFilter.value = fuelType
    }
    
    fun setVehicleFilter(vehicle: String?) {
        _vehicleFilter.value = vehicle
    }
    
    fun setDriverFilter(driver: String?) {
        _driverFilter.value = driver
    }
    
    fun setDateRange(start: LocalDateTime?, end: LocalDateTime?) {
        _dateRangeStart.value = start
        _dateRangeEnd.value = end
    }
    
    fun setSortByLatest(latest: Boolean) {
        _sortByLatest.value = latest
    }
    
    fun clearAllFilters() {
        _searchQuery.value = ""
        _fuelTypeFilter.value = null
        _vehicleFilter.value = null
        _driverFilter.value = null
        _dateRangeStart.value = null
        _dateRangeEnd.value = null
        _filterByStatus.value = "ALL"
    }
    
    fun markAsUsed(gasSlipId: String) {
        viewModelScope.launch {
            try {
                gasSlipRepository.markAsUsed(gasSlipId)
                loadAllGasSlips()
                Timber.d("Gas slip marked as used: $gasSlipId")
            } catch (e: Exception) {
                _uiState.value = GasSlipUiState.Error("Failed to mark as used: ${e.message}")
                Timber.e(e, "Error marking gas slip as used")
            }
        }
    }
    
    fun markAsDispensed(gasSlipId: String) {
        viewModelScope.launch {
            try {
                gasSlipRepository.markAsDispensed(gasSlipId)
                loadAllGasSlips()
                Timber.d("Gas slip marked as dispensed: $gasSlipId")
            } catch (e: Exception) {
                _uiState.value = GasSlipUiState.Error("Failed to mark as dispensed: ${e.message}")
                Timber.e(e, "Error marking gas slip as dispensed")
            }
        }
    }
    
    fun resetState() {
        _uiState.value = GasSlipUiState.Idle
    }
}
