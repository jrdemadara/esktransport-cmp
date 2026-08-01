package org.noztek.esktransport.feature.passenger.settings.presentation

import org.noztek.esktransport.feature.passenger.settings.domain.model.SavedPlace
import org.noztek.esktransport.feature.passenger.settings.domain.model.SavedPlaceType
import org.noztek.esktransport.feature.passenger.location_search.domain.model.GeoPoint

data class SavedPlacesUiState(
    val isLoading: Boolean = true,
    val isSubmitting: Boolean = false,
    val places: List<SavedPlace> = emptyList(),
    val errorMessage: String? = null,
    val currentLocationPoint: GeoPoint? = null,
) {
    val homePlace: SavedPlace?
        get() = places.firstOrNull { it.placeType == SavedPlaceType.Home }

    val workPlace: SavedPlace?
        get() = places.firstOrNull { it.placeType == SavedPlaceType.Work }

    val customPlaces: List<SavedPlace>
        get() = places.filter { it.placeType == SavedPlaceType.Custom }
}
