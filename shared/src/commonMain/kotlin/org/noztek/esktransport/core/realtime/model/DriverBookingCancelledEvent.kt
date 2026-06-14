package org.noztek.esktransport.core.realtime.model

data class DriverBookingCancelledEvent(
    val bookingPublicId: String,
    val passengerUserId: Long?,
    val cancelledBy: String?,
)
