package org.noztek.esktransport.feature.passenger.booking_review.domain.repository

import org.noztek.esktransport.feature.passenger.booking_review.domain.model.Booking
import org.noztek.esktransport.feature.passenger.booking_review.domain.model.BookingReviewInput

interface BookingReviewRepository {
    suspend fun createBooking(input: BookingReviewInput): Result<Booking>
}
