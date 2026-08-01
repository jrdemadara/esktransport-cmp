package org.noztek.esktransport.feature.passenger.activity.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PassengerActivityResponseDto(
    val data: PassengerActivityDashboardDto,
)

@Serializable
data class PassengerActivityDashboardDto(
    @SerialName("recent_rides")
    val recentRides: List<PassengerRideActivityDto> = emptyList(),
    @SerialName("pending_bookings")
    val pendingBookings: List<PassengerPendingBookingDto> = emptyList(),
)

@Serializable
data class PassengerRideActivityDto(
    @SerialName("booking_public_id")
    val bookingPublicId: String,
    @SerialName("booking_type")
    val bookingType: String = "ride",
    val status: String,
    @SerialName("driver_name")
    val driverName: String? = null,
    @SerialName("vehicle_type_code")
    val vehicleTypeCode: String? = null,
    @SerialName("requested_at")
    val requestedAt: String? = null,
    @SerialName("completed_at")
    val completedAt: String? = null,
    @SerialName("canceled_at")
    val canceledAt: String? = null,
    @SerialName("activity_at")
    val activityAt: String? = null,
    @SerialName("cancel_reason")
    val cancelReason: String? = null,
    val currency: String = "PHP",
    @SerialName("final_fare")
    val finalFare: Double? = null,
    @SerialName("distance_km")
    val distanceKm: Double? = null,
    @SerialName("duration_min")
    val durationMin: Int? = null,
    val pickup: PassengerActivityStopDto,
    val dropoff: PassengerActivityStopDto,
)

@Serializable
data class PassengerPendingBookingDto(
    @SerialName("booking_public_id")
    val bookingPublicId: String,
    @SerialName("booking_type")
    val bookingType: String = "ride",
    val status: String,
    @SerialName("vehicle_type_code")
    val vehicleTypeCode: String? = null,
    @SerialName("requested_at")
    val requestedAt: String? = null,
    val currency: String = "PHP",
    @SerialName("final_fare")
    val finalFare: Double? = null,
    @SerialName("pickup_label")
    val pickupLabel: String? = null,
    @SerialName("dropoff_label")
    val dropoffLabel: String? = null,
)

@Serializable
data class PassengerActivityStopDto(
    val label: String? = null,
    val lat: Double? = null,
    val lng: Double? = null,
)
