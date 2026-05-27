package org.noztek.esktransport.feature.passenger.booking_review.presentation

import org.noztek.esktransport.feature.passenger.booking_review.domain.model.BookingReviewInput

data class BookingReviewUiState(
    val input: BookingReviewInput? = null,
    val isCreatingBooking: Boolean = false,
    val isSearchingForRider: Boolean = false,
)
