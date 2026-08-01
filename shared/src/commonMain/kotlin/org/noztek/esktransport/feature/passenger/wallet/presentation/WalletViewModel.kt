package org.noztek.esktransport.feature.passenger.wallet.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.noztek.esktransport.feature.common.wallet.domain.usecase.GetWalletUseCase

class WalletViewModel(
    private val getWalletUseCase: GetWalletUseCase,
    private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {
    private val _uiState = MutableStateFlow(WalletUiState())
    val uiState = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = withContext(ioDispatcher) { getWalletUseCase() }
            result
                .onSuccess { dashboard ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            balance = dashboard.wallet.balance,
                            currency = dashboard.wallet.currency,
                            pendingTopups = dashboard.pendingTopups,
                            pendingCashouts = dashboard.pendingCashouts,
                            recentLedgerEntries = dashboard.recentLedgerEntries,
                            errorMessage = null,
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: "Unable to load wallet.",
                        )
                    }
                }
        }
    }
}
