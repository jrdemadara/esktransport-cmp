package org.noztek.esktransport.feature.passenger.settings.domain.usecase

import org.noztek.esktransport.feature.passenger.settings.domain.repository.SavedPlacesRepository

class DeleteSavedPlaceUseCase(
    private val repository: SavedPlacesRepository,
) {
    suspend operator fun invoke(id: Long): Result<Unit> = repository.deleteSavedPlace(id)
}
