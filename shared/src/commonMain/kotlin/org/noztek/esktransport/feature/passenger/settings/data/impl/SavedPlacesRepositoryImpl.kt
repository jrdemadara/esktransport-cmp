package org.noztek.esktransport.feature.passenger.settings.data.impl

import org.noztek.esktransport.core.network.ApiErrorParser
import org.noztek.esktransport.feature.passenger.settings.data.remote.SavedPlacesApi
import org.noztek.esktransport.feature.passenger.settings.data.remote.dto.toDomain
import org.noztek.esktransport.feature.passenger.settings.data.remote.dto.toRequestDto
import org.noztek.esktransport.feature.passenger.settings.domain.model.SavedPlace
import org.noztek.esktransport.feature.passenger.settings.domain.model.SavedPlacePayload
import org.noztek.esktransport.feature.passenger.settings.domain.repository.SavedPlacesRepository

class SavedPlacesRepositoryImpl(
    private val api: SavedPlacesApi,
) : SavedPlacesRepository {
    override suspend fun getSavedPlaces(): Result<List<SavedPlace>> {
        return try {
            Result.success(api.getSavedPlaces().data.map { it.toDomain() })
        } catch (throwable: Throwable) {
            val message = ApiErrorParser.parse(throwable, "Failed to load saved places.")
            Result.failure(IllegalStateException(message))
        }
    }

    override suspend fun createSavedPlace(payload: SavedPlacePayload): Result<SavedPlace> {
        return try {
            Result.success(api.createSavedPlace(payload.toRequestDto()).data.toDomain())
        } catch (throwable: Throwable) {
            val message = ApiErrorParser.parse(throwable, "Failed to save place.")
            Result.failure(IllegalStateException(message))
        }
    }

    override suspend fun updateSavedPlace(id: Long, payload: SavedPlacePayload): Result<SavedPlace> {
        return try {
            Result.success(api.updateSavedPlace(id = id, request = payload.toRequestDto()).data.toDomain())
        } catch (throwable: Throwable) {
            val message = ApiErrorParser.parse(throwable, "Failed to update saved place.")
            Result.failure(IllegalStateException(message))
        }
    }

    override suspend fun deleteSavedPlace(id: Long): Result<Unit> {
        return try {
            api.deleteSavedPlace(id)
            Result.success(Unit)
        } catch (throwable: Throwable) {
            val message = ApiErrorParser.parse(throwable, "Failed to delete saved place.")
            Result.failure(IllegalStateException(message))
        }
    }
}
