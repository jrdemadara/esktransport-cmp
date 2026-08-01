package org.noztek.esktransport.feature.passenger.home.domain.usecase

import org.noztek.esktransport.feature.passenger.home.domain.model.KnownPlace
import org.noztek.esktransport.feature.passenger.home.domain.repository.KnownPlacesRepository

class GetKnownPlacesUseCase(
    private val repository: KnownPlacesRepository,
) {
    suspend operator fun invoke(): Result<List<KnownPlace>> = repository.getKnownPlaces()
}
