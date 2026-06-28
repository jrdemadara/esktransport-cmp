package org.noztek.esktransport.feature.passenger.trip_tracking.domain.usecase

import org.noztek.esktransport.feature.passenger.trip_tracking.domain.repository.TripTrackingRepository

class CancelPassengerTripUseCase(
    private val repository: TripTrackingRepository,
) {
    suspend operator fun invoke(bookingPublicId: String): Result<Unit> {
        return repository.cancelTrip(bookingPublicId)
    }
}
