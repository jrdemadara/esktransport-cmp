package org.noztek.esktransport.feature.passenger.settings.domain.usecase

import org.noztek.esktransport.feature.passenger.settings.domain.model.SavedPlace
import org.noztek.esktransport.feature.passenger.settings.domain.model.SavedPlacePayload
import org.noztek.esktransport.feature.passenger.settings.domain.repository.SavedPlacesRepository

class CreateSavedPlaceUseCase(
    private val repository: SavedPlacesRepository,
) {
    suspend operator fun invoke(payload: SavedPlacePayload): Result<SavedPlace> = repository.createSavedPlace(payload)
}
