package org.noztek.esktransport.core.realtime.model

data class PassengerBookingOfferExpiredEvent(
    val bookingPublicId: String,
    val riderUserId: Long?,
)
