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
import org.noztek.esktransport.feature.passenger.booking_review.domain.model.Booking
import org.noztek.esktransport.feature.passenger.booking_review.domain.model.BookingReviewInput
import org.noztek.esktransport.feature.passenger.booking_review.domain.model.FareQuote
import org.noztek.esktransport.feature.passenger.booking_review.domain.usecase.CancelBookingUseCase
import org.noztek.esktransport.feature.passenger.booking_review.domain.usecase.CreateBookingUseCase
import org.noztek.esktransport.feature.passenger.booking_review.domain.usecase.CreateFareQuoteUseCase

sealed class BookingReviewUiEvent {
    data class ShowSnackbar(val message: String) : BookingReviewUiEvent()
    data class NavigateToTripTracking(val bookingId: String) : BookingReviewUiEvent()
}

class BookingReviewViewModel(
    private val createFareQuoteUseCase: CreateFareQuoteUseCase,
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
    private var fareQuoteJob: Job? = null
    private var realtimeJob: Job? = null

    fun startRealtime() {
        if (realtimeJob?.isActive == true) return
        realtimeJob = viewModelScope.launch {
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
        realtimeJob?.cancel()
        realtimeJob = null
    }

    fun setInput(input: BookingReviewInput) {
        val current = _uiState.value.input
        val isSameInput = current == null || current == input
        val shouldRequestQuote = current != input
        _uiState.value = _uiState.value.copy(
            input = input,
            fareQuote = if (shouldRequestQuote) null else _uiState.value.fareQuote,
            fareQuoteError = if (shouldRequestQuote) null else _uiState.value.fareQuoteError,
            isSearchingForRider = if (isSameInput) {
                _uiState.value.isSearchingForRider
            } else {
                false
            },
            isSearchExpired = if (isSameInput) _uiState.value.isSearchExpired else false,
            searchSecondsRemaining = if (isSameInput) _uiState.value.searchSecondsRemaining else SEARCH_TIMEOUT_SECONDS,
        )
        if (shouldRequestQuote) {
            requestFareQuote(input)
        }
    }

    fun confirmBooking() {
        val payload = _uiState.value.input ?: run {
            _uiEvents.tryEmit(BookingReviewUiEvent.ShowSnackbar("Missing booking data."))
            return
        }
        val fareQuote = _uiState.value.fareQuote ?: run {
            _uiEvents.tryEmit(BookingReviewUiEvent.ShowSnackbar("Fare is not ready yet."))
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCreatingBooking = true)
            
            val submission = withContext(ioDispatcher) {
                createBookingWithQuoteRefresh(
                    input = payload,
                    quoteId = fareQuote.id,
                )
            }
            
            submission.refreshedQuote?.let { refreshedQuote ->
                _uiState.value = _uiState.value.copy(
                    fareQuote = refreshedQuote,
                    fareQuoteError = null,
                )
            }
            _uiState.value = _uiState.value.copy(isCreatingBooking = false)

            submission.bookingResult.onSuccess { booking ->
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

    fun retryFareQuote() {
        val input = _uiState.value.input ?: return
        requestFareQuote(input)
    }

    fun showReviewSheet() {
        pendingBookingPublicId = null
        stopSearchCountdown()
        _uiState.value = _uiState.value.copy(
            isCreatingBooking = false,
            isSearchingForRider = false,
            isCancellingBooking = false,
            isSearchExpired = false,
            searchSecondsRemaining = SEARCH_TIMEOUT_SECONDS,
        )
    }

    private fun requestFareQuote(input: BookingReviewInput) {
        fareQuoteJob?.cancel()
        fareQuoteJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoadingFareQuote = true,
                fareQuoteError = null,
                fareQuote = null,
            )

            val result = withContext(ioDispatcher) {
                createFareQuoteUseCase(input)
            }

            val currentInput = _uiState.value.input
            if (currentInput != input) return@launch

            result.onSuccess { quote ->
                _uiState.value = _uiState.value.copy(
                    fareQuote = quote,
                    isLoadingFareQuote = false,
                    fareQuoteError = null,
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    fareQuote = null,
                    isLoadingFareQuote = false,
                    fareQuoteError = error.message ?: "Unable to get fare quote.",
                )
            }
        }
    }

    private suspend fun createBookingWithQuoteRefresh(
        input: BookingReviewInput,
        quoteId: Long,
    ): BookingSubmissionResult {
        val firstAttempt = createBookingUseCase(input, quoteId)
        if (!firstAttempt.isInvalidFareQuoteFailure()) {
            return BookingSubmissionResult(
                bookingResult = firstAttempt,
                refreshedQuote = null,
            )
        }

        val refreshedQuote = createFareQuoteUseCase(input).getOrElse { error ->
            return BookingSubmissionResult(
                bookingResult = Result.failure(error),
                refreshedQuote = null,
            )
        }

        return BookingSubmissionResult(
            bookingResult = createBookingUseCase(input, refreshedQuote.id),
            refreshedQuote = refreshedQuote,
        )
    }

    private fun Result<Booking>.isInvalidFareQuoteFailure(): Boolean {
        val message = exceptionOrNull()?.message.orEmpty()
        return message.contains("Fare quote is no longer valid", ignoreCase = true) ||
            message.contains("refresh the fare", ignoreCase = true)
    }

    private fun startSearchCountdown() {
        searchCountdownJob?.cancel()
        searchCountdownJob = viewModelScope.launch {
            for (remaining in SEARCH_TIMEOUT_SECONDS downTo 0) {
                _uiState.value = _uiState.value.copy(searchSecondsRemaining = remaining)
                if (remaining == 0) break
                delay(1000)
            }
            if (_uiState.value.isSearchingForRider && pendingBookingPublicId != null) {
                markSearchExpiredLocally()
            }
        }
    }

    private fun markSearchExpiredLocally() {
        pendingBookingPublicId = null
        _uiState.value = _uiState.value.copy(
            isSearchingForRider = false,
            isCancellingBooking = false,
            isSearchExpired = true,
            searchSecondsRemaining = 0,
        )
    }

    private fun stopSearchCountdown() {
        searchCountdownJob?.cancel()
        searchCountdownJob = null
    }

    override fun onCleared() {
        fareQuoteJob?.cancel()
        stopSearchCountdown()
        stopRealtime()
        super.onCleared()
    }

    private companion object {
        const val SEARCH_TIMEOUT_SECONDS = 60
    }
}

private data class BookingSubmissionResult(
    val bookingResult: Result<Booking>,
    val refreshedQuote: FareQuote?,
)
