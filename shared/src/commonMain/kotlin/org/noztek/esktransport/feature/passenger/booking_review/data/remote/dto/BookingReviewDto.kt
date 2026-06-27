package org.noztek.esktransport.feature.passenger.booking_review.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateBookingRequestDto(
    @SerialName("quote_id")
    val quoteId: Long? = null,
    @SerialName("pickup_lat")
    val pickupLat: Double,
    @SerialName("pickup_lng")
    val pickupLng: Double,
    @SerialName("pickup_label")
    val pickupLabel: String,
    @SerialName("destination_lat")
    val destinationLat: Double,
    @SerialName("destination_lng")
    val destinationLng: Double,
    @SerialName("destination_label")
    val destinationLabel: String,
    @SerialName("vehicle_type_code")
    val vehicleTypeCode: String,
    @SerialName("required_seats")
    val requiredSeats: Int,
    @SerialName("passenger_capacity")
    val passengerCapacity: Int,
    val notes: String? = null
)

@Serializable
data class CreateFareQuoteRequestDto(
    @SerialName("pickup_lat")
    val pickupLat: Double,
    @SerialName("pickup_lng")
    val pickupLng: Double,
    @SerialName("destination_lat")
    val destinationLat: Double,
    @SerialName("destination_lng")
    val destinationLng: Double,
    @SerialName("vehicle_type_code")
    val vehicleTypeCode: String,
)

@Serializable
data class CreateFareQuoteResponseDto(
    val message: String,
    val data: FareQuoteDataDto,
)

@Serializable
data class FareQuoteDataDto(
    @SerialName("quote_id")
    val quoteId: Long,
    val amount: Double,
    val currency: String,
    @SerialName("distance_km")
    val distanceKm: Double,
    @SerialName("duration_min")
    val durationMin: Int,
    @SerialName("expires_at")
    val expiresAt: String,
)

@Serializable
data class CreateBookingResponseDto(
    val message: String,
    val data: CreateBookingDataDto,
)

@Serializable
data class CreateBookingDataDto(
    @SerialName("booking_id")
    val bookingId: Int,
    @SerialName("booking_public_id")
    val bookingPublicId: String,
    val status: String,
    @SerialName("booking_type")
    val bookingType: String,
    @SerialName("final_fare")
    val finalFare: Double?,
    val currency: String,
    @SerialName("requested_at")
    val requestedAt: String,
    val pickup: BookingLocationDto,
    val destination: BookingLocationDto,
    @SerialName("required_seats")
    val requiredSeats: Int
)

@Serializable
data class BookingLocationDto(
    val label: String,
    val lat: Double,
    val lng: Double
)

@Serializable
data class CancelBookingResponseDto(
    val message: String,
    val data: CancelBookingDataDto,
)

@Serializable
data class CancelBookingDataDto(
    @SerialName("booking_public_id")
    val bookingPublicId: String,
    val status: String,
    @SerialName("cancelled_at")
    val cancelledAt: String,
)
