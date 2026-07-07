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

class EarningsViewModel(
    private val getRiderEarningsUseCase: GetRiderEarningsUseCase,
    private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {
    private val _uiState = MutableStateFlow(EarningsUiState())
    val uiState: StateFlow<EarningsUiState> = _uiState.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = withContext(ioDispatcher) {
                getRiderEarningsUseCase()
            }
            result.onSuccess { dashboard ->
                _uiState.value = EarningsUiState(dashboard = dashboard)
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = error.message ?: "Failed to load earnings.",
                )
            }
        }
    }
}
