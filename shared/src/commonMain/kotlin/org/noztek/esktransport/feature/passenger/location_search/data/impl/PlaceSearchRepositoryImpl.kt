package org.noztek.esktransport.feature.passenger.location_search.data.impl

import org.noztek.esktransport.feature.passenger.location_search.domain.model.GeoPoint
import org.noztek.esktransport.feature.passenger.location_search.domain.model.PlaceSuggestion
import org.noztek.esktransport.feature.passenger.location_search.domain.repository.PlaceSearchRepository

class PlaceSearchRepositoryImpl : PlaceSearchRepository {
    override suspend fun search(query: String, location: GeoPoint?): List<PlaceSuggestion> {
        if (query.isBlank()) return emptyList()
        val basePoint = location ?: GeoPoint(latitude = 6.6920431660391095, longitude = 124.68050838312321)
        return listOf(
            PlaceSuggestion(label = query.trim(), point = basePoint),
        )
    }
}
