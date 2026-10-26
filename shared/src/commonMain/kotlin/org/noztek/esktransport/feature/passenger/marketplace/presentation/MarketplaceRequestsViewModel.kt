package org.noztek.esktransport.feature.passenger.marketplace.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.noztek.esktransport.feature.passenger.marketplace.domain.model.MarketplaceServiceRequest
import org.noztek.esktransport.feature.passenger.marketplace.domain.repository.MarketplaceRepository
import org.noztek.esktransport.feature.passenger.marketplace.domain.usecase.GetMarketplaceRequestsUseCase

data class MarketplaceRequestsUiState(
    val requests: List<MarketplaceServiceRequest> = emptyList(),
    val isLoading: Boolean = false,
    val updatingRequestId: String? = null,
    val errorMessage: String? = null,
)

class MarketplaceRequestsViewModel(
    private val getMarketplaceRequestsUseCase: GetMarketplaceRequestsUseCase,
    private val marketplaceRepository: MarketplaceRepository,
    private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {
    private val _uiState = MutableStateFlow(MarketplaceRequestsUiState())
    val uiState = _uiState.asStateFlow()

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            withContext(ioDispatcher) { getMarketplaceRequestsUseCase() }
                .onSuccess { requests -> _uiState.value = MarketplaceRequestsUiState(requests = requests) }
                .onFailure { error -> _uiState.update { it.copy(isLoading = false, errorMessage = error.message) } }
        }
    }

    fun confirm(request: MarketplaceServiceRequest) = update(request.publicId) { marketplaceRepository.confirmRequest(it) }
    fun cancel(request: MarketplaceServiceRequest) = update(request.publicId) { marketplaceRepository.cancelRequest(it) }
    fun complete(request: MarketplaceServiceRequest) = update(request.publicId) {
        marketplaceRepository.completeRequest(it, request.quotedAmount ?: 0.0, request.currency)
    }

    private fun update(
        requestId: String,
        action: suspend (String) -> Result<MarketplaceServiceRequest>,
    ) {
        if (_uiState.value.updatingRequestId != null) return
        viewModelScope.launch {
            _uiState.update { it.copy(updatingRequestId = requestId, errorMessage = null) }
            withContext(ioDispatcher) { action(requestId) }
                .onSuccess { updated ->
                    _uiState.update { state ->
                        state.copy(
                            requests = state.requests.map { if (it.publicId == updated.publicId) updated else it },
                            updatingRequestId = null,
                        )
                    }
                }
                .onFailure { error -> _uiState.update { it.copy(updatingRequestId = null, errorMessage = error.message) } }
        }
    }
}
