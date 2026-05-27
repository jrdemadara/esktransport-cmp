package org.noztek.esktransport.feature.passenger.location_search.domain.usecase

import org.noztek.esktransport.feature.passenger.location_search.domain.model.GeoPoint
import org.noztek.esktransport.feature.passenger.location_search.domain.repository.LocationRepository

class GetCurrentLocationUseCase(
    private val locationRepository: LocationRepository,
) {
    suspend operator fun invoke(): GeoPoint? = locationRepository.getLastKnownLocation()
}
