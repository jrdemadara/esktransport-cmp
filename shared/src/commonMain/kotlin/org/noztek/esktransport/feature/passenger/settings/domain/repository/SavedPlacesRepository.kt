package org.noztek.esktransport.feature.passenger.settings.domain.repository

import org.noztek.esktransport.feature.passenger.settings.domain.model.SavedPlace
import org.noztek.esktransport.feature.passenger.settings.domain.model.SavedPlacePayload

interface SavedPlacesRepository {
    suspend fun getSavedPlaces(): Result<List<SavedPlace>>
    suspend fun createSavedPlace(payload: SavedPlacePayload): Result<SavedPlace>
    suspend fun updateSavedPlace(id: Long, payload: SavedPlacePayload): Result<SavedPlace>
    suspend fun deleteSavedPlace(id: Long): Result<Unit>
}
