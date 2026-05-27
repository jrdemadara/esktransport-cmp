package org.noztek.esktransport.feature.passenger.booking_review.presentation

data class PassengerDriverAssignedUiModel(
    val bookingPublicId: String,
    val status: String,
    val riderUserId: Long,
)