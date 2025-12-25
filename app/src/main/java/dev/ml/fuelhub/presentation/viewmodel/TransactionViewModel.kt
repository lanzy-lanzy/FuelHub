package dev.ml.fuelhub.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ml.fuelhub.data.model.FuelTransaction
import dev.ml.fuelhub.data.model.TransactionStatus
import dev.ml.fuelhub.data.model.User
import dev.ml.fuelhub.data.model.Vehicle
import dev.ml.fuelhub.domain.usecase.CreateFuelTransactionUseCase
import dev.ml.fuelhub.domain.usecase.CompleteTransactionUseCase
import dev.ml.fuelhub.domain.exception.InsufficientFuelException
import dev.ml.fuelhub.domain.exception.TransactionValidationException
import dev.ml.fuelhub.domain.exception.UnauthorizedException
import dev.ml.fuelhub.domain.repository.UserRepository
import dev.ml.fuelhub.domain.repository.VehicleRepository
import dev.ml.fuelhub.domain.repository.FuelTransactionRepository
import dev.ml.fuelhub.domain.repository.FuelWalletRepository
import dev.ml.fuelhub.domain.repository.GasSlipRepository
import dev.ml.fuelhub.data.service.NotificationService
import dev.ml.fuelhub.presentation.state.TransactionUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import java.time.LocalDateTime

/**
 * ViewModel for Transaction operations.
 * Manages transaction creation and UI state.
 */
@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val createFuelTransactionUseCase: CreateFuelTransactionUseCase,
    private val completeTransactionUseCase: CompleteTransactionUseCase,
    private val userRepository: UserRepository,
    private val vehicleRepository: VehicleRepository,
    private val transactionRepository: FuelTransactionRepository,
    private val walletRepository: FuelWalletRepository,
    private val gasSlipRepository: GasSlipRepository,
    private val notificationService: NotificationService
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
                Timber.d("=== LOADING TRANSACTIONS FROM SERVER ===")
                val transactionsList = transactionRepository.getAllTransactionsFromServer()
                _transactionHistory.value = transactionsList.sortedByDescending { it.createdAt }
                Timber.d("✓ Loaded ${transactionsList.size} transactions from server")
                if (transactionsList.isNotEmpty()) {
                    transactionsList.forEach { tx ->
                        Timber.d("  - ${tx.referenceNumber} | ${tx.vehicleId} | ${tx.status.name}")
                    }
                } else {
                    Timber.w("⚠️ NO TRANSACTIONS FOUND IN FIRESTORE!")
                }
            } catch (e: Exception) {
                Timber.e(e, "❌ Error loading transaction history: ${e.message}")
                _transactionHistory.value = emptyList()
            }
        }
    }
    
    fun refreshTransactions() {
        Timber.d("🔄 MANUAL TRANSACTION REFRESH REQUESTED")
        loadTransactionHistory()
    }
    
    /**
     * Load transactions with guaranteed Firestore sync (not cached).
     * Used by gas station operator to ensure latest data from server.
     */
    fun loadTransactionsFromFirestoreDirect() {
        viewModelScope.launch {
            try {
                Timber.d("🔄 FORCE FIRESTORE SYNC - Direct server fetch")
                // This forces a server fetch, bypassing local cache
                val transactionsList = transactionRepository.getAllTransactionsFromServer()
                _transactionHistory.value = transactionsList.sortedByDescending { it.createdAt }
                Timber.d("✓ Direct Firestore fetch completed: ${transactionsList.size} transactions")
                transactionsList.forEach { tx ->
                    Timber.d("  → ${tx.referenceNumber} (${tx.status.name})")
                }
            } catch (e: Exception) {
                Timber.e(e, "❌ Direct Firestore fetch failed")
            }
        }
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
                val transaction = output.transaction
                
                _uiState.value = TransactionUiState.Success(
                    transaction = transaction,
                    gasSlip = output.gasSlip,
                    newWalletBalance = output.newWalletBalance
                )
                
                // Notify gas stations about the new transaction
                Timber.d("🔔 Attempting to notify gas stations...")
                val vehicle = vehicleRepository.getVehicleById(vehicleId)
                Timber.d("Vehicle: ${vehicle?.plateNumber}")
                notificationService.notifyGasStationsOnTransactionCreated(
                    transactionId = transaction.id,
                    referenceNumber = transaction.referenceNumber,
                    vehiclePlateNumber = vehicle?.plateNumber ?: "Unknown",
                    litersToPump = litersToPump
                )
                Timber.d("🔔 Notification request sent")
                
                Timber.d("Transaction created successfully: ${transaction.referenceNumber}")
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
    
    /**
     * Update transaction status to DISPENSED by gas station operator.
     * Called when fuel is physically dispensed and confirmed via QR code scan.
     */
    fun confirmFuelDispensed(transactionId: String, transaction: FuelTransaction) {
        viewModelScope.launch {
            try {
                val updatedTransaction = transaction.copy(
                    status = TransactionStatus.DISPENSED,
                    completedAt = LocalDateTime.now()
                )
                transactionRepository.updateTransaction(updatedTransaction)
                
                // Also update the associated gas slip status to DISPENSED
                val gasSlip = gasSlipRepository.getGasSlipByTransactionId(transactionId)
                if (gasSlip != null) {
                    gasSlipRepository.markAsDispensed(gasSlip.id)
                    Timber.d("Gas slip ${gasSlip.id} marked as DISPENSED for transaction $transactionId")
                } else {
                    Timber.w("No gas slip found for transaction $transactionId")
                }
                
                // Send notification when fuel is successfully dispensed
                try {
                    val vehicle = vehicleRepository.getVehicleById(transaction.vehicleId)
                    val vehiclePlate = vehicle?.plateNumber ?: "Unknown"
                    
                    notificationService.notifyGasStationsOnSuccessfulDispense(
                        referenceNumber = transaction.referenceNumber,
                        litersPumped = transaction.litersToPump,
                        vehiclePlate = vehiclePlate
                    )
                    Timber.d("🔔 Dispense success notification sent for ${transaction.referenceNumber}")
                } catch (e: Exception) {
                    Timber.e(e, "Failed to send dispense notification")
                }
                
                loadTransactionHistory()
                Timber.d("Transaction $transactionId marked as DISPENSED")
            } catch (e: Exception) {
                Timber.e(e, "Error updating transaction status to DISPENSED")
            }
        }
    }
    
    /**
     * Complete a transaction and deduct fuel from wallet.
     * Called after fuel has been dispensed and transaction is ready for finalization.
     * 
     * This will:
     * 1. Verify transaction is in DISPENSED status
     * 2. Check wallet has sufficient balance
     * 3. Deduct liters from wallet
     * 4. Update transaction to COMPLETED
     */
    fun completeTransaction(transactionId: String) {
        viewModelScope.launch {
            try {
                val output = completeTransactionUseCase.execute(transactionId)
                loadTransactionHistory()
                Timber.d(
                    "Transaction $transactionId completed. " +
                    "Wallet balance: ${output.newWalletBalance} L"
                )
            } catch (e: InsufficientFuelException) {
                Timber.e(e, "Insufficient fuel: ${e.message}")
            } catch (e: Exception) {
                Timber.e(e, "Error completing transaction $transactionId")
            }
        }
    }
    
    /**
     * Check the status of a gas slip by transaction ID.
     * Used to verify if a slip has been cancelled before dispensing.
     */
    fun checkGasSlipStatus(transactionId: String, callback: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val gasSlip = gasSlipRepository.getGasSlipByTransactionId(transactionId)
                if (gasSlip != null) {
                    callback(gasSlip.status.name)
                    Timber.d("Gas slip status for $transactionId: ${gasSlip.status.name}")
                } else {
                    callback("NOT_FOUND")
                    Timber.w("Gas slip not found for transaction $transactionId")
                }
            } catch (e: Exception) {
                Timber.e(e, "Error checking gas slip status for $transactionId")
                callback("ERROR")
            }
        }
    }
}
