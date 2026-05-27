package org.noztek.esktransport.feature.passenger.location_search.domain.repository

import org.noztek.esktransport.feature.passenger.location_search.domain.model.GeoPoint

interface ReverseGeocodeRepository {
    suspend fun resolveLabel(point: GeoPoint): String?
}
