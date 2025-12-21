package dev.ml.fuelhub.presentation.state

import dev.ml.fuelhub.data.model.FuelWallet

/**
 * UI State for Wallet operations.
 */
sealed class WalletUiState {
    object Idle : WalletUiState()
    object Loading : WalletUiState()
    data class Success(val wallet: FuelWallet) : WalletUiState()
    data class Error(val message: String) : WalletUiState()
}
