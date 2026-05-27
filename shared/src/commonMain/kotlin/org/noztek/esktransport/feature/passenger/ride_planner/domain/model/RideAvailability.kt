package org.noztek.esktransport.feature.passenger.ride_planner.domain.model

data class RideAvailability(
    val pickup: Point,
    val radiusKm: Int,
    val nearbyDriversCount: Int,
    val vehicleOptions: List<VehicleOption>,
    val nearbyDrivers: List<NearbyDriver>
)

data class Point(
    val lat: Double,
    val lng: Double
)

data class VehicleOption(
    val vehicleTypeCode: String,
    val availableDrivers: Int,
    val minEtaMinutes: Int,
    val minDistanceM: Double
)

data class NearbyDriver(
    val driverPublicId: String,
    val vehicleLabel: String?,
    val vehiclePlate: String?,
    val vehicleTypeCode: String,
    val lat: Double,
    val lng: Double,
    val distanceM: Double,
    val etaMinutes: Int,
    val heading: Int,
    val passengerCapacity: Int?,
    val rating: Double,
    val estimatedFare: Double?,
    val currency: String?,
)
