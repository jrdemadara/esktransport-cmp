package org.noztek.esktransport.feature.passenger.booking_review.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.noztek.esktransport.core.realtime.passenger.PassengerRealtimeCoordinator
import org.noztek.esktransport.feature.passenger.booking_review.domain.model.BookingReviewInput
import org.noztek.esktransport.feature.passenger.booking_review.domain.usecase.CreateBookingUseCase

sealed class BookingReviewUiEvent {
    data class ShowSnackbar(val message: String) : BookingReviewUiEvent()
    data class NavigateToTripTracking(val bookingId: String) : BookingReviewUiEvent()
}

class BookingReviewViewModel(
    private val createBookingUseCase: CreateBookingUseCase,
    private val ioDispatcher: CoroutineDispatcher,
    private val realtimeCoordinator: PassengerRealtimeCoordinator,
    private val currentDriverAssigned: PassengerDriverAssignedUiModel? = null,
) : ViewModel() {
    private val _uiState = MutableStateFlow(BookingReviewUiState())
    val uiState: StateFlow<BookingReviewUiState> = _uiState.asStateFlow()

    private val _uiEvents = MutableSharedFlow<BookingReviewUiEvent>(extraBufferCapacity = 1)
    val uiEvents: SharedFlow<BookingReviewUiEvent> = _uiEvents.asSharedFlow()
    private var pendingBookingPublicId: String? = null

    fun startRealtime() {
        viewModelScope.launch {
            realtimeCoordinator.subscribePassengerDriverAssigned()
            launch {
                realtimeCoordinator.passengerDriverAssigned().collect { event ->
                    if (event.bookingPublicId == pendingBookingPublicId) {
                        _uiEvents.tryEmit(BookingReviewUiEvent.ShowSnackbar("Driver assigned. Waiting for acceptance..."))
                    }
                }
            }
            launch {
                realtimeCoordinator.passengerBookingAccepted().collect { event ->
                    println("BookingReviewVM got booking.accepted event bookingId=${event.bookingPublicId}, pending=$pendingBookingPublicId, searching=${_uiState.value.isSearchingForRider}")
                    val pendingId = pendingBookingPublicId
                    val shouldNavigate =
                        (pendingId != null && event.bookingPublicId == pendingId) ||
                            (pendingId == null && _uiState.value.isSearchingForRider)
                    if (shouldNavigate) {
                        pendingBookingPublicId = null
                        println("BookingReviewVM emitting NavigateToTripTracking bookingId=${event.bookingPublicId}")
                        _uiEvents.tryEmit(BookingReviewUiEvent.NavigateToTripTracking(event.bookingPublicId))
                    }
                }
            }
            if (currentDriverAssigned != null) {
                if (currentDriverAssigned.bookingPublicId == pendingBookingPublicId) {
                    _uiState.value = _uiState.value.copy(isSearchingForRider = false)
                }
            }
        }
    }

    fun stopRealtime() {
        realtimeCoordinator.unsubscribePassengerDriverAssigned()
    }
    fun setInput(input: BookingReviewInput) {
        val current = _uiState.value.input
        _uiState.value = _uiState.value.copy(
            input = input,
            isSearchingForRider = if (current == null || current == input) {
                _uiState.value.isSearchingForRider
            } else {
                false
            },
        )
    }

    fun confirmBooking() {
        val payload = _uiState.value.input ?: run {
            _uiEvents.tryEmit(BookingReviewUiEvent.ShowSnackbar("Missing booking data."))
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCreatingBooking = true)
            
            val result = withContext(ioDispatcher) {
                createBookingUseCase(payload)
            }
            
            _uiState.value = _uiState.value.copy(isCreatingBooking = false)

            result.onSuccess { booking ->
                println("Booking created: booking_public_id=${booking.publicId}")
                _uiEvents.tryEmit(BookingReviewUiEvent.ShowSnackbar("Booking created successfully."))
                pendingBookingPublicId = booking.publicId
                println("BookingReviewVM pending booking set to ${booking.publicId}")
                _uiState.value = _uiState.value.copy(isSearchingForRider = true)
            }.onFailure { error ->
                _uiEvents.tryEmit(BookingReviewUiEvent.ShowSnackbar("Booking failed: ${error.message}"))
            }
        }
    }
}
