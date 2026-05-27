package org.noztek.esktransport.feature.passenger.ride_planner.domain.usecase

import org.noztek.esktransport.feature.passenger.ride_planner.domain.repository.CurrentLocationRepository

class ResolveCurrentLocationLabelUseCase(
    private val repository: CurrentLocationRepository,
) {
    suspend operator fun invoke(): String? = repository.resolveCurrentLocationLabel()
}
