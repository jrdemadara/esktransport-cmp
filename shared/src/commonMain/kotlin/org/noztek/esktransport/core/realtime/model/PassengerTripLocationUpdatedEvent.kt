package org.noztek.esktransport.core.realtime.model

data class PassengerTripLocationUpdatedEvent(
    val bookingPublicId: String,
    val latitude: Double,
    val longitude: Double,
    val bearing: Double?,
    val speedKph: Double?,
    val accuracyM: Double?,
    val recordedAt: String?,
    val phase: String?,
)
