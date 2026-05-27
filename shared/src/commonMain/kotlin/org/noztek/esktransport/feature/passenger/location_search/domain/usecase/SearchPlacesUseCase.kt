package org.noztek.esktransport.feature.passenger.location_search.domain.usecase

import org.noztek.esktransport.feature.passenger.location_search.domain.model.GeoPoint
import org.noztek.esktransport.feature.passenger.location_search.domain.model.PlaceSuggestion
import org.noztek.esktransport.feature.passenger.location_search.domain.repository.PlaceSearchRepository

class SearchPlacesUseCase(
    private val placeSearchRepository: PlaceSearchRepository,
) {
    suspend operator fun invoke(query: String, location: GeoPoint?): List<PlaceSuggestion> =
        placeSearchRepository.search(query = query, location = location)
}
