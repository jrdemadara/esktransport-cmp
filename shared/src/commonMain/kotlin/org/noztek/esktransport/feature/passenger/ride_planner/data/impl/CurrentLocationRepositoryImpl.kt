package org.noztek.esktransport.feature.passenger.ride_planner.data.impl

import org.noztek.esktransport.feature.passenger.location_search.domain.model.GeoPoint
import org.noztek.esktransport.feature.passenger.ride_planner.domain.repository.CurrentLocationRepository

class CurrentLocationRepositoryImpl : CurrentLocationRepository {
    override suspend fun resolveCurrentLocationLabel(): String? = null
    override suspend fun resolveCurrentLocationPoint(): GeoPoint? = null
}
