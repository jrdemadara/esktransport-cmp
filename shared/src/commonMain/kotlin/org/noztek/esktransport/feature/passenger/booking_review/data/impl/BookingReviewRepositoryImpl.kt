package org.noztek.esktransport.feature.passenger.booking_review.data.impl

import org.noztek.esktransport.core.network.ApiErrorParser
import org.noztek.esktransport.feature.passenger.booking_review.data.remote.BookingReviewApi
import org.noztek.esktransport.feature.passenger.booking_review.data.remote.dto.CreateBookingRequestDto
import org.noztek.esktransport.feature.passenger.booking_review.domain.model.Booking
import org.noztek.esktransport.feature.passenger.booking_review.domain.model.BookingReviewInput
import org.noztek.esktransport.feature.passenger.booking_review.domain.repository.BookingReviewRepository

class BookingReviewRepositoryImpl(
    private val api: BookingReviewApi,
) : BookingReviewRepository {
    override suspend fun createBooking(input: BookingReviewInput): Result<Booking> {
        return try {
            val response = api.createBooking(
                CreateBookingRequestDto(
                    pickupLat = input.pickupPoint.latitude,
                    pickupLng = input.pickupPoint.longitude,
                    pickupLabel = input.pickupLocation,
                    destinationLat = input.destinationPoint.latitude,
                    destinationLng = input.destinationPoint.longitude,
                    destinationLabel = input.destinationLocation,
                    vehicleTypeCode = input.vehicleTypeCode,
                    requiredSeats = input.requiredSeats,
                    passengerCapacity = 1,
                    notes = input.notes ?: "No notes"
                )
            )
            
            val data = response.data
            Result.success(
                Booking(
                    id = data.bookingId,
                    publicId = data.bookingPublicId,
                    status = data.status,
                    type = data.bookingType,
                    finalFare = data.finalFare,
                    currency = data.currency,
                    requestedAt = data.requestedAt,
                    requiredSeats = data.requiredSeats
                )
            )
        } catch (throwable: Throwable) {
            val message = ApiErrorParser.parse(throwable, "Create booking failed.")
            Result.failure(IllegalStateException(message))
        }
    }
}
