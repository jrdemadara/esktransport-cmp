package org.noztek.esktransport.feature.passenger.trip_tracking.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TripTrackingResponseDto(
    val data: TripTrackingDataDto,
)

@Serializable
data class TripTrackingDataDto(
    @SerialName("booking_public_id")
    val bookingPublicId: String,
    val status: String,
    @SerialName("requested_at")
    val requestedAt: String,
    @SerialName("updated_at")
    val updatedAt: String,
    @SerialName("final_fare")
    val finalFare: Double? = null,
    val currency: String? = null,
    val pickup: TripLocationDto,
    val destination: TripLocationDto,
    val rider: RiderTripInfoDto,
    @SerialName("latest_location")
    val latestLocation: LatestLocationDto? = null,
)

@Serializable
data class RiderTripInfoDto(
    @SerialName("public_id")
    val publicId: String,
    val name: String,
    val rating: Double? = null,
    @SerialName("vehicle_type_code")
    val vehicleTypeCode: String,
    @SerialName("vehicle_label")
    val vehicleLabel: String,
    @SerialName("vehicle_plate")
    val vehiclePlate: String,
)

@Serializable
data class LatestLocationDto(
    val lat: Double,
    val lng: Double,
    val bearing: Double? = null,
    @SerialName("speed_kph")
    val speedKph: Double? = null,
    @SerialName("recorded_at")
    val recordedAt: String,
)

@Serializable
data class TripLocationDto(
    val label: String? = null,
    val lat: Double? = null,
    val lng: Double? = null,
)
