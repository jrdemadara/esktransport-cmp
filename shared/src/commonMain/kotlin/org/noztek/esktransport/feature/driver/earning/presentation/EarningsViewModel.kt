package org.noztek.esktransport.feature.driver.earning.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.noztek.esktransport.feature.driver.earning.domain.usecase.GetRiderEarningsUseCase
import org.noztek.esktransport.feature.driver.wallet.domain.usecase.GetDriverWalletUseCase

class EarningsViewModel(
    private val getRiderEarningsUseCase: GetRiderEarningsUseCase,
    private val getDriverWalletUseCase: GetDriverWalletUseCase,
    private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {
    private val _uiState = MutableStateFlow(EarningsUiState())
    val uiState: StateFlow<EarningsUiState> = _uiState.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = withContext(ioDispatcher) {
                val earnings = getRiderEarningsUseCase()
                val wallet = getDriverWalletUseCase()
                earnings to wallet
            }

            val earnings = result.first
            val wallet = result.second
            val error = earnings.exceptionOrNull() ?: wallet.exceptionOrNull()

            if (error != null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = error.message ?: "Failed to load earnings.",
                )
                return@launch
            }

            _uiState.value = EarningsUiState(
                dashboard = earnings.getOrNull(),
                walletDashboard = wallet.getOrNull(),
            )
        }
    }
}
