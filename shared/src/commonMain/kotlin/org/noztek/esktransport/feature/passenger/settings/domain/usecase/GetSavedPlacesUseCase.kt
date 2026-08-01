package org.noztek.esktransport.feature.passenger.settings.domain.usecase

import org.noztek.esktransport.feature.passenger.settings.domain.model.SavedPlace
import org.noztek.esktransport.feature.passenger.settings.domain.repository.SavedPlacesRepository

class GetSavedPlacesUseCase(
    private val repository: SavedPlacesRepository,
) {
    suspend operator fun invoke(): Result<List<SavedPlace>> = repository.getSavedPlaces()
}
