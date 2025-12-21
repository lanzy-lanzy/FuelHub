package dev.ml.fuelhub.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.ml.fuelhub.data.model.FuelTransaction
import dev.ml.fuelhub.data.model.User
import dev.ml.fuelhub.data.model.Vehicle
import dev.ml.fuelhub.domain.usecase.CreateFuelTransactionUseCase
import dev.ml.fuelhub.domain.exception.InsufficientFuelException
import dev.ml.fuelhub.domain.exception.TransactionValidationException
import dev.ml.fuelhub.domain.exception.UnauthorizedException
import dev.ml.fuelhub.domain.repository.UserRepository
import dev.ml.fuelhub.domain.repository.VehicleRepository
import dev.ml.fuelhub.domain.repository.FuelTransactionRepository
import dev.ml.fuelhub.presentation.state.TransactionUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * ViewModel for Transaction operations.
 * Manages transaction creation and UI state.
 */
class TransactionViewModel(
    private val createFuelTransactionUseCase: CreateFuelTransactionUseCase,
    private val userRepository: UserRepository,
    private val vehicleRepository: VehicleRepository,
    private val transactionRepository: FuelTransactionRepository
) : ViewModel() {
     
    private val _uiState = MutableStateFlow<TransactionUiState>(TransactionUiState.Idle)
    val uiState: StateFlow<TransactionUiState> = _uiState
     
    private val _transactionHistory = MutableStateFlow<List<FuelTransaction>>(emptyList())
    val transactionHistory: StateFlow<List<FuelTransaction>> = _transactionHistory
     
    private val _drivers = MutableStateFlow<List<User>>(emptyList())
    val drivers: StateFlow<List<User>> = _drivers
     
    private val _vehicles = MutableStateFlow<List<Vehicle>>(emptyList())
    val vehicles: StateFlow<List<Vehicle>> = _vehicles
     
    init {
        loadDrivers()
        loadVehicles()
        loadTransactionHistory()
    }
    
    fun loadDrivers() {
        viewModelScope.launch {
            try {
                val driversList = userRepository.getAllActiveUsers()
                _drivers.value = driversList
                Timber.d("Loaded ${driversList.size} drivers for transaction")
            } catch (e: Exception) {
                Timber.e(e, "Error loading drivers")
            }
        }
    }
    
    fun loadVehicles() {
        viewModelScope.launch {
            try {
                val vehiclesList = vehicleRepository.getAllActiveVehicles()
                _vehicles.value = vehiclesList
                Timber.d("Loaded ${vehiclesList.size} vehicles for transaction")
            } catch (e: Exception) {
                Timber.e(e, "Error loading vehicles")
            }
        }
    }
    
    fun loadTransactionHistory() {
        viewModelScope.launch {
            try {
                val transactionsList = transactionRepository.getAllTransactionsFromServer()
                _transactionHistory.value = transactionsList.sortedByDescending { it.createdAt }
                Timber.d("Loaded ${transactionsList.size} transactions from server")
            } catch (e: Exception) {
                Timber.e(e, "Error loading transaction history")
                _transactionHistory.value = emptyList()
            }
        }
    }
    
    fun refreshTransactions() {
        loadTransactionHistory()
    }
    
    fun createTransaction(
        vehicleId: String,
        litersToPump: Double,
        costPerLiter: Double = 0.0,
        destination: String,
        tripPurpose: String,
        passengers: String? = null,
        createdBy: String,
        walletId: String
    ) {
        viewModelScope.launch {
            _uiState.value = TransactionUiState.Loading
            try {
                val input = CreateFuelTransactionUseCase.CreateTransactionInput(
                    vehicleId = vehicleId,
                    litersToPump = litersToPump,
                    costPerLiter = costPerLiter,
                    destination = destination,
                    tripPurpose = tripPurpose,
                    passengers = passengers,
                    createdBy = createdBy,
                    walletId = walletId
                )
                
                val output = createFuelTransactionUseCase.execute(input)
                _uiState.value = TransactionUiState.Success(
                    transaction = output.transaction,
                    gasSlip = output.gasSlip,
                    newWalletBalance = output.newWalletBalance
                )
                Timber.d("Transaction created successfully: ${output.transaction.referenceNumber}")
            } catch (e: InsufficientFuelException) {
                _uiState.value = TransactionUiState.Error("Insufficient fuel: ${e.message}")
                Timber.e("Insufficient fuel: ${e.message}")
            } catch (e: TransactionValidationException) {
                _uiState.value = TransactionUiState.Error("Validation error: ${e.message}")
                Timber.e("Validation error: ${e.message}")
            } catch (e: UnauthorizedException) {
                _uiState.value = TransactionUiState.Error("Unauthorized: ${e.message}")
                Timber.e("Unauthorized: ${e.message}")
            } catch (e: Exception) {
                _uiState.value = TransactionUiState.Error("Error: ${e.message}")
                Timber.e(e, "Unexpected error creating transaction")
            }
        }
    }
    
    fun resetState() {
        _uiState.value = TransactionUiState.Idle
    }
}
