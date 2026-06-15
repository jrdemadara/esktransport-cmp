package org.noztek.esktransport.feature.driver.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.noztek.esktransport.core.realtime.driver.DriverBookingOfferRealtime
import org.noztek.esktransport.feature.common.active_booking.domain.model.ActiveBookingStatus
import org.noztek.esktransport.feature.common.active_booking.domain.usecase.GetDriverActiveBookingUseCase
import org.noztek.esktransport.feature.driver.home.domain.usecase.AcceptDriverHomeOfferUseCase
import org.noztek.esktransport.feature.driver.home.domain.usecase.ExpireDriverHomeOfferUseCase
import org.noztek.esktransport.feature.driver.home.domain.usecase.GetDriverAvailabilityUseCase
import org.noztek.esktransport.feature.driver.home.domain.usecase.SetDriverAvailabilityUseCase

private const val DefaultPassengerName = "Passenger"

data class DriverHomeUiState(
    val isAvailable: Boolean = false,
    val isSubmitting: Boolean = false,
    val isAcceptingOffer: Boolean = false,
    val statusMessage: String? = null,
    val currentOffer: DriverHomeBookingOfferUiModel? = null,
)

sealed class DriverHomeUiEvent {
    data class ShowSnackbar(val message: String) : DriverHomeUiEvent()
    data class NavigateToTrip(val bookingPublicId: String) : DriverHomeUiEvent()
}

class DriverHomeViewModel(
    private val getDriverAvailabilityUseCase: GetDriverAvailabilityUseCase,
    private val setDriverAvailabilityUseCase: SetDriverAvailabilityUseCase,
    private val acceptDriverHomeOfferUseCase: AcceptDriverHomeOfferUseCase,
    private val expireDriverHomeOfferUseCase: ExpireDriverHomeOfferUseCase,
    private val getDriverActiveBookingUseCase: GetDriverActiveBookingUseCase,
    private val realtimeCoordinator: DriverBookingOfferRealtime,
    private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {
    private val _uiState = MutableStateFlow(DriverHomeUiState())
    val uiState: StateFlow<DriverHomeUiState> = _uiState.asStateFlow()

    private val _uiEvents = MutableSharedFlow<DriverHomeUiEvent>()
    val uiEvents: SharedFlow<DriverHomeUiEvent> = _uiEvents

    init {
        refreshAvailability()
    }

    fun startRealtime() {
        viewModelScope.launch {
            realtimeCoordinator.subscribeDriverBookingOffers()
            launch {
                realtimeCoordinator.driverBookingOffers().collect { event ->
                    _uiState.update {
                        it.copy(
                            currentOffer = DriverHomeBookingOfferUiModel(
                                bookingPublicId = event.bookingPublicId,
                                passengerName = event.passengerName ?: DefaultPassengerName,
                                pickupLabel = event.pickupLabel,
                                destinationLabel = event.destinationLabel,
                                fareLabel = event.finalFare?.let(::formatFare) ?: "N/A",
                            ),
                        )
                    }
                }
            }
            launch {
                realtimeCoordinator.driverBookingCancelled().collect { event ->
                    val currentOffer = _uiState.value.currentOffer
                    if (currentOffer?.bookingPublicId == event.bookingPublicId) {
                        _uiState.update {
                            it.copy(
                                currentOffer = null,
                                isAcceptingOffer = false,
                            )
                        }
                        println("DriverHome dismissed cancelled booking offer: ${event.bookingPublicId}")
                    }
                }
            }
        }
    }

    fun stopRealtime() {
        realtimeCoordinator.unsubscribeDriverBookingOffers()
    }

    fun refreshAvailability() {
        viewModelScope.launch {
            val result = withContext(ioDispatcher) { getDriverAvailabilityUseCase() }
            result.onSuccess { available ->
                _uiState.update { it.copy(isAvailable = available) }
            }.onFailure { error ->
                val message = error.message ?: "Failed to fetch driver availability."
                _uiState.update { state -> state.copy(statusMessage = message) }
                _uiEvents.tryEmit(DriverHomeUiEvent.ShowSnackbar(message))
                println("DriverHome availability sync error: $message")
            }
        }
    }

    fun restoreActiveBooking() {
        viewModelScope.launch {
            val result = withContext(ioDispatcher) { getDriverActiveBookingUseCase() }
            result.onSuccess { activeBooking ->
                when (activeBooking?.status) {
                    ActiveBookingStatus.OFFERED -> {
                        _uiState.update {
                            it.copy(
                                currentOffer = DriverHomeBookingOfferUiModel(
                                    bookingPublicId = activeBooking.bookingPublicId,
                                    passengerName = activeBooking.passenger?.name ?: DefaultPassengerName,
                                    pickupLabel = activeBooking.pickup?.label ?: "Pickup",
                                    destinationLabel = activeBooking.destination?.label ?: "Destination",
                                    fareLabel = activeBooking.finalFare?.let(::formatFare) ?: "N/A",
                                ),
                                isAcceptingOffer = false,
                            )
                        }
                    }
                    ActiveBookingStatus.ACCEPTED,
                    ActiveBookingStatus.ARRIVING_PICKUP,
                    ActiveBookingStatus.IN_PROGRESS -> {
                        _uiEvents.tryEmit(DriverHomeUiEvent.NavigateToTrip(activeBooking.bookingPublicId))
                    }
                    else -> Unit
                }
            }.onFailure { error ->
                println("DriverHome active booking restore error: ${error.message}")
            }
        }
    }

    fun onGoToggle() {
        updateAvailability(target = !_uiState.value.isAvailable)
    }

    fun goOfflineOnAppBackground() {
        val state = _uiState.value
        if (state.isAvailable && !state.isSubmitting) {
            updateAvailability(target = false)
        }
    }

    private fun updateAvailability(target: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, statusMessage = null) }
            val result = withContext(ioDispatcher) { setDriverAvailabilityUseCase(target) }
            result.onSuccess { available ->
                val message = if (available) "You're now online." else "You're now offline."
                _uiState.update { it.copy(isAvailable = available, isSubmitting = false, statusMessage = message) }
                _uiEvents.tryEmit(DriverHomeUiEvent.ShowSnackbar(message))
            }.onFailure { error ->
                val message = error.message ?: "Failed to update driver availability."
                _uiState.update { state -> state.copy(isSubmitting = false, statusMessage = message) }
                _uiEvents.tryEmit(DriverHomeUiEvent.ShowSnackbar(message))
                println("DriverHome availability error: $message")
            }
        }
    }

    fun dismissOfferSheet() {
        _uiState.update { it.copy(currentOffer = null, isAcceptingOffer = false) }
    }

    fun expireCurrentOffer() {
        val offer = _uiState.value.currentOffer ?: return
        if (_uiState.value.isAcceptingOffer) return

        _uiState.update { it.copy(currentOffer = null, isAcceptingOffer = false) }
        viewModelScope.launch {
            val result = withContext(ioDispatcher) {
                expireDriverHomeOfferUseCase(offer.bookingPublicId)
            }
            result.onFailure { error ->
                println("DriverHome offer timeout error: ${error.message}")
            }
        }
    }

    fun showMockIncomingOffer() {
        _uiState.update {
            it.copy(
                currentOffer = DriverHomeBookingOfferUiModel(
                    bookingPublicId = "mock-booking-offer-001",
                    passengerName = "Passenger",
                    pickupLabel = "New Castle, Bachatle 3982",
                    destinationLabel = "Beza Building, aadis 3259",
                    fareLabel = "₱184.50",
                ),
            )
        }
    }

    fun acceptCurrentOffer() {
        val offer = _uiState.value.currentOffer ?: return
        if (_uiState.value.isAcceptingOffer) return

        viewModelScope.launch {
            _uiState.update { it.copy(isAcceptingOffer = true) }
            val result = withContext(ioDispatcher) {
                acceptDriverHomeOfferUseCase(offer.bookingPublicId)
            }
            result.onSuccess {
                _uiState.update { it.copy(isAcceptingOffer = false, currentOffer = null) }
                _uiEvents.emit(DriverHomeUiEvent.NavigateToTrip(offer.bookingPublicId))
            }.onFailure { error ->
                _uiState.update { it.copy(isAcceptingOffer = false) }
                _uiEvents.emit(DriverHomeUiEvent.ShowSnackbar(error.message ?: "Failed to accept booking."))
            }
        }
    }

    override fun onCleared() {
        stopRealtime()
        super.onCleared()
    }

    private fun formatFare(value: Double): String {
        val cents = ((value * 100.0).toLong()).coerceAtLeast(0L)
        val pesos = cents / 100
        val centavos = cents % 100
        return "PHP $pesos.${centavos.toString().padStart(2, '0')}"
    }
}
