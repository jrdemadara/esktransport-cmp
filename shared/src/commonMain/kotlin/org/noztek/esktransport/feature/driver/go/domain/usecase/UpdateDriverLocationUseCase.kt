package org.noztek.esktransport.feature.driver.go.domain.usecase

import org.noztek.esktransport.feature.driver.go.domain.repository.GoRepository
import org.noztek.esktransport.feature.passenger.location_search.domain.model.GeoPoint

class UpdateDriverLocationUseCase(
    private val repository: GoRepository,
) {
    suspend operator fun invoke(point: GeoPoint): Result<Unit> = repository.updateLocation(point)
}
