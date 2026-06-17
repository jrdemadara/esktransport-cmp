package org.noztek.esktransport.feature.driver.go.domain.usecase

import org.noztek.esktransport.feature.driver.go.domain.repository.GoRepository

class SetDriverAvailabilityUseCase(
    private val repository: GoRepository,
) {
    suspend operator fun invoke(isAvailable: Boolean): Result<Boolean> = repository.setAvailability(isAvailable)
}
