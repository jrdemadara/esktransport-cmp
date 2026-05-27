package org.noztek.esktransport.feature.passenger.ride_planner.domain.usecase

import org.noztek.esktransport.feature.passenger.location_search.domain.model.GeoPoint
import org.noztek.esktransport.feature.passenger.ride_planner.domain.repository.CurrentLocationRepository

class ResolveCurrentLocationPointUseCase(
    private val repository: CurrentLocationRepository,
) {
    suspend operator fun invoke(): GeoPoint? = repository.resolveCurrentLocationPoint()
}
