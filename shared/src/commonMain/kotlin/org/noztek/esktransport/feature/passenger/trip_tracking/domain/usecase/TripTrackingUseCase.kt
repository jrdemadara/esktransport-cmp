package org.noztek.esktransport.feature.passenger.trip_tracking.domain.usecase

import org.noztek.esktransport.feature.passenger.trip_tracking.domain.model.TripTrackingSession
import org.noztek.esktransport.feature.passenger.trip_tracking.domain.repository.TripTrackingRepository

class TripTrackingUseCase(
    private val repository: TripTrackingRepository
) {
    suspend operator fun invoke(bookingPublicId: String): TripTrackingSession {
        return repository.getTripTrackingSession(bookingPublicId)
    }
}
