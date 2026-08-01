package org.noztek.esktransport.feature.passenger.activity.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.noztek.esktransport.feature.common.active_booking.domain.usecase.GetPassengerActiveBookingUseCase
import org.noztek.esktransport.feature.passenger.activity.domain.usecase.GetPassengerActivityUseCase

class ActivityViewModel(
    private val getPassengerActiveBookingUseCase: GetPassengerActiveBookingUseCase,
    private val getPassengerActivityUseCase: GetPassengerActivityUseCase,
    private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ActivityUiState())
    val uiState = _uiState.asStateFlow()

    init {
        refreshActiveBooking()
        refreshActivity()
    }

    fun refreshActiveBooking() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingActiveBooking = true, errorMessage = null) }
            val result = withContext(ioDispatcher) { getPassengerActiveBookingUseCase() }
            result
                .onSuccess { activeBooking ->
                    _uiState.update {
                        it.copy(
                            isLoadingActiveBooking = false,
                            activeBooking = activeBooking,
                            errorMessage = null,
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoadingActiveBooking = false,
                            errorMessage = throwable.message ?: "Unable to load active booking.",
                        )
                    }
                }
        }
    }

    fun refreshActivity() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingActivity = true, errorMessage = null) }
            val result = withContext(ioDispatcher) { getPassengerActivityUseCase() }
            result
                .onSuccess { dashboard ->
                    _uiState.update {
                        it.copy(
                            isLoadingActivity = false,
                            recentRides = dashboard.recentRides,
                            pendingBookings = dashboard.pendingBookings,
                            errorMessage = null,
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoadingActivity = false,
                            errorMessage = throwable.message ?: "Unable to load activity.",
                        )
                    }
                }
        }
    }
}
