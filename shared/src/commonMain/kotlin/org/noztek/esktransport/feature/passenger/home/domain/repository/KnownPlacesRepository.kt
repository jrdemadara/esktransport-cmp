package org.noztek.esktransport.feature.passenger.home.domain.repository

import org.noztek.esktransport.feature.passenger.home.domain.model.KnownPlace

interface KnownPlacesRepository {
    suspend fun getKnownPlaces(): Result<List<KnownPlace>>
}
