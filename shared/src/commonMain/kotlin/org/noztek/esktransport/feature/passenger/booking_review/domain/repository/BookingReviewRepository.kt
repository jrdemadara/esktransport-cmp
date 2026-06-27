package org.noztek.esktransport.feature.passenger.booking_review.domain.repository

import org.noztek.esktransport.feature.passenger.booking_review.domain.model.Booking
import org.noztek.esktransport.feature.passenger.booking_review.domain.model.BookingReviewInput
import org.noztek.esktransport.feature.passenger.booking_review.domain.model.FareQuote

interface BookingReviewRepository {
    suspend fun createFareQuote(input: BookingReviewInput): Result<FareQuote>
    suspend fun createBooking(input: BookingReviewInput, quoteId: Long?): Result<Booking>
    suspend fun cancelBooking(bookingPublicId: String): Result<Unit>
}
