package org.noztek.esktransport.feature.passenger.location_search.data.impl

import org.noztek.esktransport.feature.passenger.location_search.domain.model.GeoPoint
import org.noztek.esktransport.feature.passenger.location_search.domain.repository.LocationRepository

class LocationRepositoryImpl : LocationRepository {
    override suspend fun getLastKnownLocation(): GeoPoint? = null
}
