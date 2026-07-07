package org.noztek.esktransport.feature.passenger.trip_tracking.domain.usecase

import org.noztek.esktransport.feature.passenger.trip_tracking.domain.repository.TripTrackingRepository

class SubmitPassengerTripFeedbackUseCase(
    private val repository: TripTrackingRepository,
) {
    suspend operator fun invoke(
        bookingPublicId: String,
        rating: Int,
        comment: String?,
    ): Result<Unit> = repository.submitFeedback(
        bookingPublicId = bookingPublicId,
        rating = rating,
        comment = comment,
    )
}
