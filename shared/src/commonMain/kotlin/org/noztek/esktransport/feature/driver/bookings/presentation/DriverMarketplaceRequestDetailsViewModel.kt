package org.noztek.esktransport.feature.driver.bookings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.noztek.esktransport.feature.driver.bookings.domain.model.DriverMarketplaceRequest
import org.noztek.esktransport.feature.driver.bookings.domain.repository.DriverMarketplaceInquiriesRepository

data class DriverMarketplaceRequestDetailsUiState(
    val isLoading: Boolean = false,
    val request: DriverMarketplaceRequest? = null,
    val isUpdating: Boolean = false,
    val errorMessage: String? = null,
)

class DriverMarketplaceRequestDetailsViewModel(
    private val repository: DriverMarketplaceInquiriesRepository,
    private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {
    private val _uiState = MutableStateFlow(DriverMarketplaceRequestDetailsUiState())
    val uiState = _uiState.asStateFlow()

    fun load(requestPublicId: String) {
        if (requestPublicId.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            withContext(ioDispatcher) { repository.getRequest(requestPublicId) }
                .onSuccess { request -> _uiState.value = DriverMarketplaceRequestDetailsUiState(request = request) }
                .onFailure { error -> _uiState.value = DriverMarketplaceRequestDetailsUiState(errorMessage = error.message) }
        }
    }

    fun accept(quotedAmount: Double) = update { id -> repository.acceptRequest(id, quotedAmount, "PHP", null) }
    fun start() = update { id -> repository.startRequest(id) }
    fun complete() = update { id -> repository.completeRequest(id) }

    private fun update(action: suspend (String) -> Result<Unit>) {
        val id = _uiState.value.request?.publicId ?: return
        if (_uiState.value.isUpdating) return
        viewModelScope.launch {
            _uiState.update { it.copy(isUpdating = true, errorMessage = null) }
            withContext(ioDispatcher) { action(id) }
                .onSuccess { load(id) }
                .onFailure { error -> _uiState.update { it.copy(isUpdating = false, errorMessage = error.message) } }
        }
    }
}
