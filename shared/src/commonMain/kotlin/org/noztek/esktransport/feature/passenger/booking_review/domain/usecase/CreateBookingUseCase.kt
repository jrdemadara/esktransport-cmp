package org.noztek.esktransport.feature.passenger.booking_review.domain.usecase

import org.noztek.esktransport.feature.passenger.booking_review.domain.model.Booking
import org.noztek.esktransport.feature.passenger.booking_review.domain.model.BookingReviewInput
import org.noztek.esktransport.feature.passenger.booking_review.domain.repository.BookingReviewRepository

class CreateBookingUseCase(
    private val repository: BookingReviewRepository,
) {
    suspend operator fun invoke(input: BookingReviewInput): Result<Booking> {
        return repository.createBooking(input)
    }
}
