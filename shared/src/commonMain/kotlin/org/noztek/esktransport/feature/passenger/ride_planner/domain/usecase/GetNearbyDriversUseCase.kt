package org.noztek.esktransport.feature.passenger.ride_planner.domain.usecase

import org.noztek.esktransport.feature.passenger.ride_planner.domain.model.RideAvailability
import org.noztek.esktransport.feature.passenger.ride_planner.domain.model.RideAvailabilityPayload
import org.noztek.esktransport.feature.passenger.ride_planner.domain.repository.RidePlannerRepository

class GetNearbyDriversUseCase(
    private val repository: RidePlannerRepository
) {
    suspend operator fun invoke(payload: RideAvailabilityPayload): Result<RideAvailability> {
        return repository.getNearbyDrivers(payload)
    }
}
