package org.noztek.esktransport.feature.driver.go.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
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
import org.noztek.esktransport.feature.driver.go.domain.lifecycle.DriverAvailabilityLifecycleCoordinator
import org.noztek.esktransport.feature.driver.go.domain.usecase.AcceptOfferUseCase
import org.noztek.esktransport.feature.driver.go.domain.usecase.ExpireOfferUseCase
import org.noztek.esktransport.feature.driver.go.domain.usecase.GetDriverAvailabilityUseCase
import org.noztek.esktransport.feature.driver.go.domain.usecase.SetDriverAvailabilityUseCase

private const val DefaultPassengerName = "Passenger"

data class GoUiState(
    val isAvailable: Boolean = false,
    val isSubmitting: Boolean = false,
    val pendingAvailability: Boolean? = null,
    val isAcceptingOffer: Boolean = false,
    val statusMessage: String? = null,
    val currentOffer: GoBookingOfferUiModel? = null,
)

sealed class GoUiEvent {
    data class ShowSnackbar(val message: String) : GoUiEvent()
    data class NavigateToTrip(val bookingPublicId: String) : GoUiEvent()
}

class GoViewModel(
    private val getDriverAvailabilityUseCase: GetDriverAvailabilityUseCase,
    private val setDriverAvailabilityUseCase: SetDriverAvailabilityUseCase,
    private val acceptOfferUseCase: AcceptOfferUseCase,
    private val expireOfferUseCase: ExpireOfferUseCase,
    private val getActiveBookingUseCase: GetDriverActiveBookingUseCase,
    private val realtimeCoordinator: DriverBookingOfferRealtime,
    private val availabilityLifecycleCoordinator: DriverAvailabilityLifecycleCoordinator,
    private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {
    private val _uiState = MutableStateFlow(GoUiState())
    val uiState: StateFlow<GoUiState> = _uiState.asStateFlow()

    private val _uiEvents = MutableSharedFlow<GoUiEvent>()
    val uiEvents: SharedFlow<GoUiEvent> = _uiEvents
    private var realtimeJob: Job? = null

    init {
        refreshAvailability()
    }

    fun startRealtime() {
        if (realtimeJob?.isActive == true) return
        realtimeJob = viewModelScope.launch {
            realtimeCoordinator.subscribeDriverBookingOffers()
            launch {
                realtimeCoordinator.driverBookingOffers().collect { event ->
                    _uiState.update {
                        it.copy(
                            currentOffer = GoBookingOfferUiModel(
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
                        println("Go dismissed cancelled booking offer: ${event.bookingPublicId}")
                    }
                }
            }
        }
    }

    fun stopRealtime() {
        realtimeJob?.cancel()
        realtimeJob = null
    }

    fun refreshAvailability() {
        viewModelScope.launch {
            val result = withContext(ioDispatcher) { getDriverAvailabilityUseCase() }
            result.onSuccess { available ->
                availabilityLifecycleCoordinator.updateAvailability(available)
                _uiState.update { it.copy(isAvailable = available) }
            }.onFailure { error ->
                val message = error.message ?: "Failed to fetch driver availability."
                _uiState.update { state -> state.copy(statusMessage = message) }
                _uiEvents.tryEmit(GoUiEvent.ShowSnackbar(message))
                println("Go availability sync error: $message")
            }
        }
    }

    fun restoreActiveBooking() {
        viewModelScope.launch {
            val result = withContext(ioDispatcher) { getActiveBookingUseCase() }
            result.onSuccess { activeBooking ->
                when (activeBooking?.status) {
                    ActiveBookingStatus.OFFERED -> {
                        _uiState.update {
                            it.copy(
                                currentOffer = GoBookingOfferUiModel(
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
                        _uiEvents.tryEmit(GoUiEvent.NavigateToTrip(activeBooking.bookingPublicId))
                    }
                    else -> Unit
                }
            }.onFailure { error ->
                println("Go active booking restore error: ${error.message}")
            }
        }
    }

    fun onGoToggle() {
        updateAvailability(target = !_uiState.value.isAvailable)
    }

    fun goOffline() {
        val state = _uiState.value
        if (!state.isAvailable || state.isSubmitting) return

        updateAvailability(target = false)
    }

    fun goOfflineAndExit(onExit: () -> Unit) {
        val state = _uiState.value
        if (state.isSubmitting) return

        if (!state.isAvailable) {
            onExit()
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSubmitting = true,
                    pendingAvailability = false,
                    statusMessage = null,
                )
            }
            val result = withContext(ioDispatcher) { setDriverAvailabilityUseCase(false) }
            result.onSuccess {
                availabilityLifecycleCoordinator.updateAvailability(false)
                onExit()
            }.onFailure { error ->
                val message = error.message ?: "Failed to go offline."
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        pendingAvailability = null,
                        statusMessage = message,
                    )
                }
                _uiEvents.tryEmit(GoUiEvent.ShowSnackbar(message))
                println("Go offline before exit error: $message")
            }
        }
    }

    fun goOfflineOnAppBackground() {
        val state = _uiState.value
        if (state.isAvailable && !state.isSubmitting) {
            updateAvailability(target = false)
        }
    }

    private fun updateAvailability(target: Boolean) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSubmitting = true,
                    pendingAvailability = target,
                    statusMessage = null,
                )
            }
            val result = withContext(ioDispatcher) { setDriverAvailabilityUseCase(target) }
            result.onSuccess { available ->
                availabilityLifecycleCoordinator.updateAvailability(available)
                val message = if (available) "You're now online." else "You're now offline."
                _uiState.update {
                    it.copy(
                        isAvailable = available,
                        isSubmitting = false,
                        pendingAvailability = null,
                        statusMessage = message,
                    )
                }
                _uiEvents.tryEmit(GoUiEvent.ShowSnackbar(message))
            }.onFailure { error ->
                val message = error.message ?: "Failed to update driver availability."
                _uiState.update { state ->
                    state.copy(
                        isSubmitting = false,
                        pendingAvailability = null,
                        statusMessage = message,
                    )
                }
                _uiEvents.tryEmit(GoUiEvent.ShowSnackbar(message))
                println("Go availability error: $message")
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
                expireOfferUseCase(offer.bookingPublicId)
            }
            result.onFailure { error ->
                println("Go offer timeout error: ${error.message}")
            }
        }
    }

    fun acceptCurrentOffer() {
        val offer = _uiState.value.currentOffer ?: return
        if (_uiState.value.isAcceptingOffer) return

        viewModelScope.launch {
            _uiState.update { it.copy(isAcceptingOffer = true) }
            val result = withContext(ioDispatcher) {
                acceptOfferUseCase(offer.bookingPublicId)
            }
            result.onSuccess {
                _uiState.update { it.copy(isAcceptingOffer = false, currentOffer = null) }
                _uiEvents.emit(GoUiEvent.NavigateToTrip(offer.bookingPublicId))
            }.onFailure { error ->
                _uiState.update { it.copy(isAcceptingOffer = false) }
                _uiEvents.emit(GoUiEvent.ShowSnackbar(error.message ?: "Failed to accept booking."))
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
