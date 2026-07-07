package org.noztek.esktransport.feature.rider.trip_navigation.domain.usecase

import org.noztek.esktransport.feature.rider.trip_navigation.domain.repository.RiderTripNavigationRepository

class SubmitRiderTripFeedbackUseCase(
    private val repository: RiderTripNavigationRepository,
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
