package org.noztek.esktransport.feature.rider.trip_navigation.domain.usecase

import org.noztek.esktransport.feature.rider.trip_navigation.domain.repository.RiderTripNavigationRepository

class CompleteRiderTripUseCase(
    private val repository: RiderTripNavigationRepository,
) {
    suspend operator fun invoke(bookingPublicId: String): Result<Unit> {
        return repository.completeTrip(bookingPublicId)
    }
}
