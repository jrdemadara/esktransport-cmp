package org.noztek.esktransport.feature.rider.trip_navigation.domain.usecase

import org.noztek.esktransport.feature.rider.trip_navigation.domain.repository.RiderTripNavigationRepository

class CancelRiderTripUseCase(
    private val repository: RiderTripNavigationRepository,
) {
    suspend operator fun invoke(bookingPublicId: String): Result<Unit> {
        return repository.cancelBooking(bookingPublicId)
    }
}
