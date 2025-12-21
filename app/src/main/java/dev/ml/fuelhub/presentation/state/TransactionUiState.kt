package dev.ml.fuelhub.presentation.state

import dev.ml.fuelhub.data.model.FuelTransaction
import dev.ml.fuelhub.data.model.GasSlip

/**
 * UI State for Transaction operations.
 */
sealed class TransactionUiState {
    object Idle : TransactionUiState()
    object Loading : TransactionUiState()
    data class Success(
        val transaction: FuelTransaction,
        val gasSlip: GasSlip,
        val newWalletBalance: Double
    ) : TransactionUiState()
    data class Error(val message: String) : TransactionUiState()
}
