package org.noztek.esktransport.feature.passenger.home.presentation

import org.noztek.esktransport.feature.passenger.home.domain.model.KnownPlace
import org.noztek.esktransport.feature.passenger.settings.domain.model.SavedPlace

data class PassengerHomeUiState(
    val isLoading: Boolean = true,
    val savedPlaces: List<SavedPlace> = emptyList(),
    val knownPlaces: List<KnownPlace> = emptyList(),
    val errorMessage: String? = null,
)
