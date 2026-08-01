package org.noztek.esktransport.feature.passenger.settings.domain.usecase

import org.noztek.esktransport.feature.passenger.settings.domain.model.SavedPlace
import org.noztek.esktransport.feature.passenger.settings.domain.model.SavedPlacePayload
import org.noztek.esktransport.feature.passenger.settings.domain.repository.SavedPlacesRepository

class UpdateSavedPlaceUseCase(
    private val repository: SavedPlacesRepository,
) {
    suspend operator fun invoke(id: Long, payload: SavedPlacePayload): Result<SavedPlace> {
        return repository.updateSavedPlace(id = id, payload = payload)
    }
}
