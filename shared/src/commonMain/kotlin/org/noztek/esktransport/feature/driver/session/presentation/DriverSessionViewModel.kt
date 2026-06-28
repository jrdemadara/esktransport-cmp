package org.noztek.esktransport.feature.driver.session.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.noztek.esktransport.feature.common.active_booking.domain.model.ActiveBookingStatus
import org.noztek.esktransport.feature.common.active_booking.domain.usecase.GetDriverActiveBookingUseCase

sealed class DriverSessionUiEvent {
    data object NavigateToGoScreen : DriverSessionUiEvent()
    data class NavigateToTripNavigation(val bookingPublicId: String) : DriverSessionUiEvent()
}

class DriverSessionViewModel(
    private val getDriverActiveBookingUseCase: GetDriverActiveBookingUseCase,
    private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {
    private val _uiEvents = MutableSharedFlow<DriverSessionUiEvent>()
    val uiEvents: SharedFlow<DriverSessionUiEvent> = _uiEvents

    fun restoreActiveBooking() {
        viewModelScope.launch {
            val result = withContext(ioDispatcher) {
                getDriverActiveBookingUseCase()
            }
            result.onSuccess { activeBooking ->
                when (activeBooking?.status) {
                    ActiveBookingStatus.OFFERED -> {
                        _uiEvents.emit(DriverSessionUiEvent.NavigateToGoScreen)
                    }
                    ActiveBookingStatus.ACCEPTED,
                    ActiveBookingStatus.ARRIVING_PICKUP,
                    ActiveBookingStatus.IN_PROGRESS -> {
                        _uiEvents.emit(
                            DriverSessionUiEvent.NavigateToTripNavigation(activeBooking.bookingPublicId),
                        )
                    }
                    else -> Unit
                }
            }.onFailure {
                println("Driver active booking restore error: ${it.message}")
            }
        }
    }
}
