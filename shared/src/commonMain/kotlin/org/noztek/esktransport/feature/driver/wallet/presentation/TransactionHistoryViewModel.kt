package org.noztek.esktransport.feature.driver.wallet.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.noztek.esktransport.feature.driver.wallet.domain.usecase.GetDriverWalletUseCase

class TransactionHistoryViewModel(
    private val getDriverWalletUseCase: GetDriverWalletUseCase,
    private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {
    private val _uiState = MutableStateFlow(TransactionHistoryUiState())
    val uiState = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = withContext(ioDispatcher) { getDriverWalletUseCase() }
            result
                .onSuccess { dashboard ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            entries = dashboard.recentLedgerEntries,
                            errorMessage = null,
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: "Unable to load transactions.",
                        )
                    }
                }
        }
    }
}
