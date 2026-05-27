package org.noztek.esktransport.feature.passenger.ride_planner.domain.model

data class RideAvailabilityPayload(
    val pickupLat: Double,
    val pickupLng: Double,
    val destinationLat: Double,
    val destinationLng: Double,
    val radiusKm: Int,
    val vehicleType: String
)
