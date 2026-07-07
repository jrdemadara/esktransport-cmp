package org.noztek.esktransport.core.realtime.model

data class PassengerTripCompletedEvent(
    val bookingPublicId: String,
    val riderUserId: Long?,
    val finalFare: Double?,
    val currency: String?,
    val completedAt: String?,
)
