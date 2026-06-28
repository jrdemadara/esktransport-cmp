package org.noztek.esktransport.core.realtime.model

data class PassengerBookingCancelledEvent(
    val bookingPublicId: String,
    val riderUserId: Long?,
    val cancelledBy: String?,
)
