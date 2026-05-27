package org.noztek.esktransport.feature.passenger.location_search.domain.model

data class PlaceSuggestion(
    val label: String,
    val point: GeoPoint,
)
