package org.noztek.esktransport.feature.common.active_booking.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ActiveBookingResponseDto(
    val data: ActiveBookingDataDto? = null,
)

@Serializable
data class ActiveBookingDataDto(
    @SerialName("booking_public_id") val bookingPublicId: String,
    val status: String,
    @SerialName("final_fare") val finalFare: Double? = null,
    val currency: String? = null,
    @SerialName("requested_at") val requestedAt: String? = null,
    @SerialName("search_expires_at") val searchExpiresAt: String? = null,
    @SerialName("offer_expires_at") val offerExpiresAt: String? = null,
    val pickup: ActiveBookingPointDto? = null,
    val destination: ActiveBookingPointDto? = null,
    val driver: ActiveBookingDriverDto? = null,
    val passenger: ActiveBookingPassengerDto? = null,
)

@Serializable
data class ActiveBookingPointDto(
    val label: String? = null,
    val lat: Double? = null,
    val lng: Double? = null,
)

@Serializable
data class ActiveBookingDriverDto(
    @SerialName("rider_user_id") val riderUserId: Long? = null,
    @SerialName("driver_public_id") val driverPublicId: String? = null,
    val name: String? = null,
    @SerialName("vehicle_type_code") val vehicleTypeCode: String? = null,
    @SerialName("vehicle_label") val vehicleLabel: String? = null,
    @SerialName("vehicle_plate") val vehiclePlate: String? = null,
    @SerialName("passenger_capacity") val passengerCapacity: Int? = null,
)

@Serializable
data class ActiveBookingPassengerDto(
    @SerialName("user_id") val userId: Long? = null,
    val name: String? = null,
)
