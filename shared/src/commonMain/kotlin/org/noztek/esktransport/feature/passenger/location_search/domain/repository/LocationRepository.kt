package org.noztek.esktransport.feature.passenger.location_search.domain.repository

import org.noztek.esktransport.feature.passenger.location_search.domain.model.GeoPoint

interface LocationRepository {
    suspend fun getLastKnownLocation(): GeoPoint?
}
