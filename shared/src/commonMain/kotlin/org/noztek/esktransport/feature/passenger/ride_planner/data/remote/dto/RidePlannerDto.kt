package org.noztek.esktransport.feature.passenger.ride_planner.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RidePlannerRequestDto(
    @SerialName(value = "pickup_lat")
    val pickupLat: Double,
    @SerialName(value = "pickup_lng")
    val pickupLng: Double,
    @SerialName(value = "destination_lat")
    val destinationLat: Double,
    @SerialName(value = "destination_lng")
    val destinationLng: Double,
    @SerialName(value = "radius_km")
    val radiusKm: Int,
    @SerialName(value = "vehicle_type")
    val vehicleType: String,
)

@Serializable
data class RidePlannerResponseDto(
    val data: RidePlannerDataDto
)

@Serializable
data class RidePlannerDataDto(
    val pickup: RidePlannerPickupDto,
    @SerialName(value = "radius_km")
    val radiusKm: Int,
    @SerialName(value = "nearby_drivers_count")
    val nearbyDriversCount: Int,
    @SerialName(value = "vehicle_options")
    val vehicleOptions: List<RidePlannerVehicleOptionDto> = emptyList(),
    @SerialName(value = "nearby_drivers")
    val nearbyDrivers: List<RidePlannerNearbyDriverDto> = emptyList()
)

@Serializable
data class RidePlannerPickupDto(
    val lat: Double,
    val lng: Double
)

@Serializable
data class RidePlannerVehicleOptionDto(
    @SerialName(value = "vehicle_type_code")
    val vehicleTypeCode: String,
    @SerialName(value = "available_drivers")
    val availableDrivers: Int,
    @SerialName(value = "min_eta_minutes")
    val minEtaMinutes: Int,
    @SerialName(value = "min_distance_m")
    val minDistanceM: Double
)

@Serializable
data class RidePlannerNearbyDriverDto(
    @SerialName(value = "driver_public_id")
    val driverPublicId: String,
    @SerialName(value = "vehicle_label")
    val vehicleLabel: String? = null,
    @SerialName(value = "vehicle_plate")
    val vehiclePlate: String? = null,
    @SerialName(value = "vehicle_type_code")
    val vehicleTypeCode: String,
    val lat: Double,
    val lng: Double,
    @SerialName(value = "distance_m")
    val distanceM: Double,
    @SerialName(value = "eta_minutes")
    val etaMinutes: Int,
    val heading: Int,
    @SerialName(value = "passenger_capacity")
    val passengerCapacity: Int? = null,
    val rating: Double,
    @SerialName(value = "estimated_fare")
    val estimatedFare: Double? = null,
    val currency: String? = null,
)
