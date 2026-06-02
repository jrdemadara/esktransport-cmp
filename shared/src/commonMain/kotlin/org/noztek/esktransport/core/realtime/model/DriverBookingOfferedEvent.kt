package org.noztek.esktransport.core.realtime.model

data class DriverBookingOfferedEvent(
    val bookingPublicId: String,
    val passengerUserId: Long?,
    val passengerName: String?,
    val pickupLabel: String,
    val pickupLat: Double?,
    val pickupLng: Double?,
    val destinationLabel: String,
    val destinationLat: Double?,
    val destinationLng: Double?,
    val finalFare: Double?,
)
