package dev.ml.fuelhub.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.ml.fuelhub.domain.repository.FuelWalletRepository
import dev.ml.fuelhub.presentation.state.WalletUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * ViewModel for Wallet operations.
 * Manages wallet balance monitoring and refill operations.
 */
class WalletViewModel(
    private val walletRepository: FuelWalletRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<WalletUiState>(WalletUiState.Idle)
    val uiState: StateFlow<WalletUiState> = _uiState
    
    fun loadWallet(walletId: String) {
        viewModelScope.launch {
            _uiState.value = WalletUiState.Loading
            try {
                val wallet = walletRepository.getWalletById(walletId)
                if (wallet != null) {
                    _uiState.value = WalletUiState.Success(wallet)
                    Timber.d("Wallet loaded: $walletId")
                } else {
                    _uiState.value = WalletUiState.Error("Wallet not found")
                    Timber.w("Wallet not found: $walletId")
                }
            } catch (e: Exception) {
                _uiState.value = WalletUiState.Error("Error loading wallet: ${e.message}")
                Timber.e(e, "Error loading wallet")
            }
        }
    }
    
    fun refillWallet(walletId: String, additionalLiters: Double) {
        viewModelScope.launch {
            _uiState.value = WalletUiState.Loading
            try {
                val updated = walletRepository.refillWallet(walletId, additionalLiters)
                _uiState.value = WalletUiState.Success(updated)
                Timber.d("Wallet refilled: $walletId, added: $additionalLiters L")
            } catch (e: Exception) {
                _uiState.value = WalletUiState.Error("Error refilling wallet: ${e.message}")
                Timber.e(e, "Error refilling wallet")
            }
        }
    }
    
    fun resetState() {
        _uiState.value = WalletUiState.Idle
    }
}
