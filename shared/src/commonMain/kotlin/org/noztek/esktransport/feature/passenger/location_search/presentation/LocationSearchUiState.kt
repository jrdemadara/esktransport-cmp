package org.noztek.esktransport.feature.passenger.location_search.presentation

import org.noztek.esktransport.feature.passenger.location_search.domain.model.GeoPoint
import org.noztek.esktransport.feature.passenger.location_search.domain.model.PlaceSuggestion

data class LocationSearchUiState(
    val query: String = "",
    val suggestions: List<PlaceSuggestion> = emptyList(),
    val tappedLocationLabel: String? = null,
    val currentLocationPoint: GeoPoint? = null,
    val selectedPoint: GeoPoint? = null,
)
