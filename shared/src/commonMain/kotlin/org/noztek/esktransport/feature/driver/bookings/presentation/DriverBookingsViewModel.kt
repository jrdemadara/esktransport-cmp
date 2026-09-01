package org.noztek.esktransport.feature.driver.bookings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.noztek.esktransport.feature.common.marketplace_chat.domain.model.MarketplaceChatParticipantRole
import org.noztek.esktransport.feature.common.marketplace_chat.domain.usecase.ObserveMarketplaceChatMessagesUseCase
import org.noztek.esktransport.feature.common.marketplace_chat.domain.usecase.SubscribeMarketplaceChatUseCase
import org.noztek.esktransport.feature.driver.bookings.domain.usecase.AcceptDriverMarketplaceRequestUseCase
import org.noztek.esktransport.feature.driver.bookings.domain.usecase.GetDriverMarketplaceInquiriesUseCase

class DriverBookingsViewModel(
    private val getDriverMarketplaceInquiriesUseCase: GetDriverMarketplaceInquiriesUseCase,
    private val acceptDriverMarketplaceRequestUseCase: AcceptDriverMarketplaceRequestUseCase,
    private val subscribeMarketplaceChatUseCase: SubscribeMarketplaceChatUseCase,
    private val observeMarketplaceChatMessagesUseCase: ObserveMarketplaceChatMessagesUseCase,
    private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {
    private val _uiState = MutableStateFlow(DriverBookingsUiState())
    val uiState: StateFlow<DriverBookingsUiState> = _uiState.asStateFlow()
    private var realtimeJob: Job? = null

    fun load(showLoading: Boolean = true) {
        viewModelScope.launch {
            if (showLoading) {
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            }
            val result = withContext(ioDispatcher) {
                getDriverMarketplaceInquiriesUseCase()
            }
            result
                .onSuccess { inquiries ->
                    _uiState.value = _uiState.value.copy(
                        inquiries = inquiries,
                        isLoading = false,
                        errorMessage = null,
                    )
                }
                .onFailure { throwable ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = throwable.message ?: "Failed to load bookings.",
                    )
                }
        }
    }

    fun subscribe() {
        subscribeMarketplaceChatUseCase(MarketplaceChatParticipantRole.Owner)
        if (realtimeJob?.isActive == true) return

        realtimeJob = viewModelScope.launch {
            observeMarketplaceChatMessagesUseCase().collectLatest {
                load(showLoading = false)
            }
        }
    }

    fun selectFilter(filter: DriverBookingFilter) {
        _uiState.value = _uiState.value.copy(selectedFilter = filter)
    }

    fun acceptRequest(requestPublicId: String) {
        if (requestPublicId.isBlank()) return
        if (_uiState.value.acceptingRequestPublicId != null) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                acceptingRequestPublicId = requestPublicId,
                errorMessage = null,
            )
            val result = withContext(ioDispatcher) {
                acceptDriverMarketplaceRequestUseCase(requestPublicId)
            }
            result
                .onSuccess {
                    _uiState.value = _uiState.value.copy(acceptingRequestPublicId = null)
                    load(showLoading = false)
                }
                .onFailure { throwable ->
                    _uiState.value = _uiState.value.copy(
                        acceptingRequestPublicId = null,
                        errorMessage = throwable.message ?: "Failed to accept request.",
                    )
                }
        }
    }
}
