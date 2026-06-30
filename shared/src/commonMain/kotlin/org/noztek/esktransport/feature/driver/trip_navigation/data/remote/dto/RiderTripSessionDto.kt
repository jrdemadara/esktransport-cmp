package org.noztek.esktransport.feature.rider.trip_navigation.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RiderTripSessionResponseDto(
    val data: RiderTripSessionDataDto,
)

@Serializable
data class RiderTripSessionDataDto(
    @SerialName("booking_public_id")
    val bookingPublicId: String,
    val status: String,
    @SerialName("final_fare")
    val finalFare: Double? = null,
    val currency: String? = null,
    @SerialName("passenger_name")
    val passengerName: String,
    val pickup: RiderTripLocationDto,
    val destination: RiderTripLocationDto,
    @SerialName("rider_current")
    val riderCurrent: RiderTripCoordinatesDto? = null,
)

@Serializable
data class RiderTripLocationDto(
    val label: String? = null,
    val lat: Double? = null,
    val lng: Double? = null,
)

@Serializable
data class RiderTripCoordinatesDto(
    val lat: Double? = null,
    val lng: Double? = null,
)
