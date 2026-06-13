package org.noztek.esktransport.feature.passenger.booking_review.domain.usecase

import org.noztek.esktransport.feature.passenger.booking_review.domain.repository.BookingReviewRepository

class CancelBookingUseCase(
    private val repository: BookingReviewRepository,
) {
    suspend operator fun invoke(bookingPublicId: String): Result<Unit> {
        return repository.cancelBooking(bookingPublicId)
    }
}
