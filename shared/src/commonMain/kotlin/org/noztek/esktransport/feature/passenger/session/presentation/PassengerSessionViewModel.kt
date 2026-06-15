package org.noztek.esktransport.feature.passenger.session.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.noztek.esktransport.feature.common.active_booking.domain.model.ActiveBookingStatus
import org.noztek.esktransport.feature.common.active_booking.domain.usecase.GetPassengerActiveBookingUseCase

sealed class PassengerSessionUiEvent {
    data class NavigateToBookingStatus(val bookingPublicId: String) : PassengerSessionUiEvent()
    data class NavigateToTripTracking(val bookingPublicId: String) : PassengerSessionUiEvent()
}

class PassengerSessionViewModel(
    private val getPassengerActiveBookingUseCase: GetPassengerActiveBookingUseCase,
    private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {
    private val _uiEvents = MutableSharedFlow<PassengerSessionUiEvent>()
    val uiEvents: SharedFlow<PassengerSessionUiEvent> = _uiEvents

    fun restoreActiveBooking() {
        viewModelScope.launch {
            val result = withContext(ioDispatcher) {
                getPassengerActiveBookingUseCase()
            }
            result.onSuccess { activeBooking ->
                when (activeBooking?.status) {
                    ActiveBookingStatus.SEARCHING,
                    ActiveBookingStatus.OFFERED -> {
                        _uiEvents.emit(
                            PassengerSessionUiEvent.NavigateToBookingStatus(activeBooking.bookingPublicId),
                        )
                    }
                    ActiveBookingStatus.ACCEPTED,
                    ActiveBookingStatus.ARRIVING_PICKUP,
                    ActiveBookingStatus.IN_PROGRESS -> {
                        _uiEvents.emit(
                            PassengerSessionUiEvent.NavigateToTripTracking(activeBooking.bookingPublicId),
                        )
                    }
                    else -> Unit
                }
            }.onFailure {
                println("Passenger active booking restore error: ${it.message}")
            }
        }
    }
}
