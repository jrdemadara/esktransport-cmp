package org.noztek.esktransport.feature.rider.trip_navigation.domain.usecase

import org.noztek.esktransport.feature.rider.trip_navigation.domain.model.RiderTripSession
import org.noztek.esktransport.feature.rider.trip_navigation.domain.repository.RiderTripNavigationRepository

class GetRiderTripSessionUseCase(
    private val repository: RiderTripNavigationRepository,
) {
    suspend operator fun invoke(bookingPublicId: String): Result<RiderTripSession> {
        return repository.getTripSession(bookingPublicId)
    }
}
