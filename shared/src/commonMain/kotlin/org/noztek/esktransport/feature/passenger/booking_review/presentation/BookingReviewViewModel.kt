package org.noztek.esktransport.feature.passenger.booking_review.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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
import org.noztek.esktransport.feature.passenger.booking_review.domain.usecase.CancelBookingUseCase
import org.noztek.esktransport.feature.passenger.booking_review.domain.usecase.CreateBookingUseCase

sealed class BookingReviewUiEvent {
    data class ShowSnackbar(val message: String) : BookingReviewUiEvent()
    data class NavigateToTripTracking(val bookingId: String) : BookingReviewUiEvent()
}

class BookingReviewViewModel(
    private val createBookingUseCase: CreateBookingUseCase,
    private val cancelBookingUseCase: CancelBookingUseCase,
    private val ioDispatcher: CoroutineDispatcher,
    private val realtimeCoordinator: PassengerRealtimeCoordinator,
    private val currentDriverAssigned: PassengerDriverAssignedUiModel? = null,
) : ViewModel() {
    private val _uiState = MutableStateFlow(BookingReviewUiState())
    val uiState: StateFlow<BookingReviewUiState> = _uiState.asStateFlow()

    private val _uiEvents = MutableSharedFlow<BookingReviewUiEvent>(extraBufferCapacity = 1)
    val uiEvents: SharedFlow<BookingReviewUiEvent> = _uiEvents.asSharedFlow()
    private var pendingBookingPublicId: String? = null
    private var searchCountdownJob: Job? = null

    fun startRealtime() {
        viewModelScope.launch {
            realtimeCoordinator.subscribePassengerDriverAssigned()
            launch {
                realtimeCoordinator.passengerDriverAssigned().collect { event ->
                    if (event.bookingPublicId == pendingBookingPublicId) {
                        println("BookingReviewVM got booking.driver_assigned event bookingId=${event.bookingPublicId}, pending=$pendingBookingPublicId")
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
                        stopSearchCountdown()
                        println("BookingReviewVM emitting NavigateToTripTracking bookingId=${event.bookingPublicId}")
                        _uiEvents.tryEmit(BookingReviewUiEvent.NavigateToTripTracking(event.bookingPublicId))
                    }
                }
            }
            launch {
                realtimeCoordinator.passengerBookingOfferExpired().collect { event ->
                    if (event.bookingPublicId == pendingBookingPublicId) {
                        println("BookingReviewVM got booking.offer_expired event bookingId=${event.bookingPublicId}; continuing search")
                        _uiState.value = _uiState.value.copy(isSearchingForRider = true)
                    }
                }
            }
            launch {
                realtimeCoordinator.passengerBookingSearchExpired().collect { event ->
                    if (event.bookingPublicId == pendingBookingPublicId) {
                        println("BookingReviewVM got booking.search_expired event bookingId=${event.bookingPublicId}")
                        pendingBookingPublicId = null
                        stopSearchCountdown()
                        _uiState.value = _uiState.value.copy(
                            isSearchingForRider = false,
                            isCancellingBooking = false,
                            isSearchExpired = true,
                            searchSecondsRemaining = 0,
                        )
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
        val isSameInput = current == null || current == input
        _uiState.value = _uiState.value.copy(
            input = input,
            isSearchingForRider = if (isSameInput) {
                _uiState.value.isSearchingForRider
            } else {
                false
            },
            isSearchExpired = if (isSameInput) _uiState.value.isSearchExpired else false,
            searchSecondsRemaining = if (isSameInput) _uiState.value.searchSecondsRemaining else SEARCH_TIMEOUT_SECONDS,
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
                pendingBookingPublicId = booking.publicId
                println("BookingReviewVM pending booking set to ${booking.publicId}")
                _uiState.value = _uiState.value.copy(
                    isSearchingForRider = true,
                    isCancellingBooking = false,
                    isSearchExpired = false,
                    searchSecondsRemaining = SEARCH_TIMEOUT_SECONDS,
                )
                startSearchCountdown()
            }.onFailure { error ->
                _uiEvents.tryEmit(BookingReviewUiEvent.ShowSnackbar("Booking failed: ${error.message}"))
            }
        }
    }

    fun cancelSearch() {
        val bookingPublicId = pendingBookingPublicId
        if (bookingPublicId == null) {
            stopSearchCountdown()
            _uiState.value = _uiState.value.copy(
                isSearchingForRider = false,
                isCancellingBooking = false,
                isSearchExpired = false,
                searchSecondsRemaining = SEARCH_TIMEOUT_SECONDS,
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCancellingBooking = true)

            val result = withContext(ioDispatcher) {
                cancelBookingUseCase(bookingPublicId)
            }

            result.onSuccess {
                pendingBookingPublicId = null
                stopSearchCountdown()
                _uiState.value = _uiState.value.copy(
                    isSearchingForRider = false,
                    isCancellingBooking = false,
                    isSearchExpired = false,
                    searchSecondsRemaining = SEARCH_TIMEOUT_SECONDS,
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(isCancellingBooking = false)
                _uiEvents.tryEmit(BookingReviewUiEvent.ShowSnackbar(error.message ?: "Cancel booking failed."))
            }
        }
    }

    fun retryExpiredSearch() {
        pendingBookingPublicId = null
        stopSearchCountdown()
        _uiState.value = _uiState.value.copy(
            isSearchExpired = false,
            isSearchingForRider = false,
            isCancellingBooking = false,
            searchSecondsRemaining = SEARCH_TIMEOUT_SECONDS,
        )
        confirmBooking()
    }

    private fun startSearchCountdown() {
        searchCountdownJob?.cancel()
        searchCountdownJob = viewModelScope.launch {
            for (remaining in SEARCH_TIMEOUT_SECONDS downTo 0) {
                _uiState.value = _uiState.value.copy(searchSecondsRemaining = remaining)
                if (remaining == 0) break
                delay(1000)
            }
        }
    }

    private fun stopSearchCountdown() {
        searchCountdownJob?.cancel()
        searchCountdownJob = null
    }

    override fun onCleared() {
        stopSearchCountdown()
        stopRealtime()
        super.onCleared()
    }

    private companion object {
        const val SEARCH_TIMEOUT_SECONDS = 60
    }
}
