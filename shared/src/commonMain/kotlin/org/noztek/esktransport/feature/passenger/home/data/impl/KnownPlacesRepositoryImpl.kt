package org.noztek.esktransport.feature.passenger.home.data.impl

import org.noztek.esktransport.core.network.ApiErrorParser
import org.noztek.esktransport.feature.passenger.home.data.remote.KnownPlacesApi
import org.noztek.esktransport.feature.passenger.home.data.remote.dto.toDomain
import org.noztek.esktransport.feature.passenger.home.domain.model.KnownPlace
import org.noztek.esktransport.feature.passenger.home.domain.repository.KnownPlacesRepository

class KnownPlacesRepositoryImpl(
    private val api: KnownPlacesApi,
) : KnownPlacesRepository {
    override suspend fun getKnownPlaces(): Result<List<KnownPlace>> {
        return try {
            Result.success(api.getKnownPlaces().data.map { it.toDomain() })
        } catch (throwable: Throwable) {
            val message = ApiErrorParser.parse(throwable, "Failed to load places.")
            Result.failure(IllegalStateException(message))
        }
    }
}
