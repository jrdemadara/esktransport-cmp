package org.noztek.esktransport.feature.passenger.booking_review.domain.model

data class Booking(
    val id: Int,
    val publicId: String,
    val status: String,
    val type: String,
    val finalFare: Double?,
    val currency: String,
    val requestedAt: String,
    val requiredSeats: Int,
)
