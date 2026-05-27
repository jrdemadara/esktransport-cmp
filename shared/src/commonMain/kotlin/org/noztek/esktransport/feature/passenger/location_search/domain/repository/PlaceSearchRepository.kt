package org.noztek.esktransport.feature.passenger.location_search.domain.repository

import org.noztek.esktransport.feature.passenger.location_search.domain.model.GeoPoint
import org.noztek.esktransport.feature.passenger.location_search.domain.model.PlaceSuggestion

interface PlaceSearchRepository {
    suspend fun search(query: String, location: GeoPoint?): List<PlaceSuggestion>
}
