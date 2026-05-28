package org.noztek.esktransport.feature.passenger.ride_planner.data.impl

import org.noztek.esktransport.core.location.CurrentLocationProvider
import org.noztek.esktransport.feature.passenger.location_search.domain.model.GeoPoint
import org.noztek.esktransport.feature.passenger.ride_planner.domain.repository.CurrentLocationRepository

class CurrentLocationRepositoryImpl(
    private val currentLocationProvider: CurrentLocationProvider,
) : CurrentLocationRepository {
    override suspend fun resolveCurrentLocationLabel(): String? = currentLocationProvider.getCurrentLocationLabel()
    override suspend fun resolveCurrentLocationPoint(): GeoPoint? = currentLocationProvider.getLastKnownLocation()
}
