package org.noztek.esktransport.feature.passenger.booking_review.presentation

import org.noztek.esktransport.feature.passenger.booking_review.domain.model.BookingReviewInput
import org.noztek.esktransport.feature.passenger.booking_review.domain.model.FareQuote

data class BookingReviewUiState(
    val input: BookingReviewInput? = null,
    val fareQuote: FareQuote? = null,
    val isLoadingFareQuote: Boolean = false,
    val fareQuoteError: String? = null,
    val isCreatingBooking: Boolean = false,
    val isSearchingForRider: Boolean = false,
    val isCancellingBooking: Boolean = false,
    val isSearchExpired: Boolean = false,
    val searchSecondsRemaining: Int = 60,
)
