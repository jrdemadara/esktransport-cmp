package org.noztek.esktransport.feature.passenger.booking_review.domain.model

data class FareQuote(
    val id: Long,
    val amount: Double,
    val currency: String,
    val distanceKm: Double,
    val durationMin: Int,
    val expiresAt: String,
)
