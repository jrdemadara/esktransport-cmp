package org.noztek.esktransport.feature.passenger.ride_planner.domain.repository

import org.noztek.esktransport.feature.passenger.location_search.domain.model.GeoPoint

interface CurrentLocationRepository {
    suspend fun resolveCurrentLocationLabel(): String?
    suspend fun resolveCurrentLocationPoint(): GeoPoint?
}
