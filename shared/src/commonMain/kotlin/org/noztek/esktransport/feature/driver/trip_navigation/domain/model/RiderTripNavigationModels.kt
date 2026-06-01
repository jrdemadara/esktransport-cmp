package org.noztek.esktransport.feature.rider.trip_navigation.domain.model

enum class RiderTripPhase {
    TO_PICKUP,
    TO_DESTINATION,
}

data class RiderTripPoint(
    val latitude: Double,
    val longitude: Double,
)

data class RiderTripSession(
    val bookingPublicId: String,
    val phase: RiderTripPhase,
    val passengerName: String,
    val pickupLabel: String,
    val destinationLabel: String,
    val riderCurrentPoint: RiderTripPoint?,
    val pickupPoint: RiderTripPoint,
    val destinationPoint: RiderTripPoint,
)
