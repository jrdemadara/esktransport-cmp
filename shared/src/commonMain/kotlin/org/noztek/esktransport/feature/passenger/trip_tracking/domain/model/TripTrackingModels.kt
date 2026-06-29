package org.noztek.esktransport.feature.passenger.trip_tracking.domain.model

data class TripTrackingSession(
    val bookingPublicId: String,
    val status: String,
    val pickupPoint: TripPoint,
    val destinationPoint: TripPoint,
    val riderInfo: RiderTripInfo,
    val latestLocation: LatestLocation?
)

data class TripPoint(
    val label: String,
    val latitude: Double?,
    val longitude: Double?
)

data class RiderTripInfo(
    val publicId: String,
    val name: String,
    val rating: Double?,
    val vehicleType: String,
    val vehicleLabel: String,
    val vehiclePlate: String
)

data class LatestLocation(
    val latitude: Double,
    val longitude: Double,
    val bearing: Double?,
    val speedKph: Double?
)
