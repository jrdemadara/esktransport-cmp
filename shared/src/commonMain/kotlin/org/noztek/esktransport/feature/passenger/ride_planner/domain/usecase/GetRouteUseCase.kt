package org.noztek.esktransport.feature.passenger.ride_planner.domain.usecase

import org.noztek.esktransport.feature.passenger.location_search.domain.model.GeoPoint
import org.noztek.esktransport.feature.passenger.ride_planner.domain.repository.RidePlannerRepository

class GetRouteUseCase(
    private val repository: RidePlannerRepository
) {
    suspend operator fun invoke(origin: GeoPoint, destination: GeoPoint): Result<List<GeoPoint>> {
        return repository.getRoute(origin, destination)
    }
}
