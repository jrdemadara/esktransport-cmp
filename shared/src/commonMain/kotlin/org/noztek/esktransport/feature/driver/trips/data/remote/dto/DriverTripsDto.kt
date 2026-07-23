package org.noztek.esktransport.feature.driver.trips.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DriverTripsResponseDto(
    val data: DriverTripsDashboardDto,
)

@Serializable
data class DriverTripsDashboardDto(
    val currency: String = "PHP",
    val summary: DriverTripsSummaryDto,
    val trips: List<DriverTripDto> = emptyList(),
)

@Serializable
data class DriverTripsSummaryDto(
    @SerialName("completed_trips")
    val completedTrips: Int = 0,
    @SerialName("online_seconds")
    val onlineSeconds: Long = 0,
    @SerialName("gross_fare")
    val grossFare: Double = 0.0,
    @SerialName("platform_fee")
    val platformFee: Double = 0.0,
    @SerialName("net_earning")
    val netEarning: Double = 0.0,
    val from: String? = null,
    val to: String? = null,
)

@Serializable
data class DriverTripDto(
    @SerialName("booking_public_id")
    val bookingPublicId: String,
    @SerialName("booking_type")
    val bookingType: String = "ride",
    val status: String,
    @SerialName("passenger_name")
    val passengerName: String = "Passenger",
    @SerialName("vehicle_type_code")
    val vehicleTypeCode: String? = null,
    @SerialName("requested_at")
    val requestedAt: String? = null,
    @SerialName("assigned_at")
    val assignedAt: String? = null,
    @SerialName("accepted_at")
    val acceptedAt: String? = null,
    @SerialName("pickup_confirmed_at")
    val pickupConfirmedAt: String? = null,
    @SerialName("completed_at")
    val completedAt: String? = null,
    @SerialName("canceled_at")
    val canceledAt: String? = null,
    @SerialName("cancel_reason")
    val cancelReason: String? = null,
    val currency: String = "PHP",
    @SerialName("final_fare")
    val finalFare: Double? = null,
    @SerialName("payment_method")
    val paymentMethod: String? = null,
    @SerialName("distance_km")
    val distanceKm: Double? = null,
    @SerialName("duration_min")
    val durationMin: Int? = null,
    val pickup: DriverTripStopDto,
    val dropoff: DriverTripStopDto,
    val settlement: DriverTripSettlementDto? = null,
    val feedback: DriverTripFeedbackBundleDto? = null,
)

@Serializable
data class DriverTripStopDto(
    val label: String? = null,
    val lat: Double? = null,
    val lng: Double? = null,
)

@Serializable
data class DriverTripSettlementDto(
    @SerialName("public_id")
    val publicId: String,
    @SerialName("gross_fare")
    val grossFare: Double? = null,
    @SerialName("platform_fee")
    val platformFee: Double = 0.0,
    @SerialName("net_earning")
    val netEarning: Double = 0.0,
    @SerialName("platform_fee_percentage")
    val platformFeePercentage: Double = 0.0,
    @SerialName("settled_at")
    val settledAt: String? = null,
)

@Serializable
data class DriverTripFeedbackBundleDto(
    @SerialName("passenger_to_driver")
    val passengerToDriver: DriverTripFeedbackDto? = null,
    @SerialName("driver_to_passenger")
    val driverToPassenger: DriverTripFeedbackDto? = null,
)

@Serializable
data class DriverTripFeedbackDto(
    val rating: Int,
    val comment: String? = null,
    @SerialName("submitted_at")
    val submittedAt: String? = null,
)
