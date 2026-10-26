package org.noztek.esktransport.feature.driver.bookings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.noztek.esktransport.feature.common.marketplace_chat.domain.model.MarketplaceChatParticipantRole
import org.noztek.esktransport.feature.common.marketplace_chat.domain.usecase.ObserveMarketplaceChatMessagesUseCase
import org.noztek.esktransport.feature.common.marketplace_chat.domain.usecase.SubscribeMarketplaceChatUseCase
import org.noztek.esktransport.feature.driver.bookings.domain.usecase.AcceptDriverMarketplaceRequestUseCase
import org.noztek.esktransport.feature.driver.bookings.domain.usecase.GetDriverMarketplaceInquiriesUseCase
import org.noztek.esktransport.feature.driver.bookings.domain.repository.DriverMarketplaceInquiriesRepository

class DriverBookingsViewModel(
    private val getDriverMarketplaceInquiriesUseCase: GetDriverMarketplaceInquiriesUseCase,
    private val acceptDriverMarketplaceRequestUseCase: AcceptDriverMarketplaceRequestUseCase,
    private val marketplaceRepository: DriverMarketplaceInquiriesRepository,
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
                        bookings = inquiries.bookings,
                        inquiries = inquiries.inquiries,
                        badgeCount = inquiries.badgeCount,
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

    fun markThreadRead(threadId: Long) {
        viewModelScope.launch {
            withContext(ioDispatcher) { marketplaceRepository.markThreadRead(threadId) }
            _uiState.update { state ->
                val updated = state.inquiries.map { inquiry ->
                    if (inquiry.id == threadId) inquiry.copy(unreadCount = 0, hasUnread = false) else inquiry
                }
                state.copy(
                    inquiries = updated,
                    badgeCount = (state.badgeCount - if (state.inquiries.any { it.id == threadId && it.hasUnread }) 1 else 0).coerceAtLeast(0),
                )
            }
        }
    }

    fun acceptRequest(requestPublicId: String, quotedAmount: Double) {
        if (requestPublicId.isBlank()) return
        if (_uiState.value.acceptingRequestPublicId != null) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                acceptingRequestPublicId = requestPublicId,
                errorMessage = null,
            )
            val result = withContext(ioDispatcher) {
                acceptDriverMarketplaceRequestUseCase(requestPublicId, quotedAmount)
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

    fun startRequest(id: String) = updateRequest(id) { marketplaceRepository.startRequest(id) }
    fun completeRequest(id: String) = updateRequest(id) { marketplaceRepository.completeRequest(id) }

    private fun updateRequest(id: String, action: suspend () -> Result<Unit>) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(acceptingRequestPublicId = id, errorMessage = null)
            withContext(ioDispatcher) { action() }
                .onSuccess { _uiState.value = _uiState.value.copy(acceptingRequestPublicId = null); load(false) }
                .onFailure { error -> _uiState.value = _uiState.value.copy(acceptingRequestPublicId = null, errorMessage = error.message) }
        }
    }
}
