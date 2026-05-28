package org.noztek.esktransport.core.location

import org.noztek.esktransport.feature.passenger.location_search.domain.model.GeoPoint

interface CurrentLocationProvider {
    suspend fun getLastKnownLocation(): GeoPoint?
    suspend fun getCurrentLocationLabel(): String?
}
