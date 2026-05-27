package org.noztek.esktransport.feature.driver.home.domain.usecase

import org.noztek.esktransport.feature.driver.home.domain.repository.DriverHomeRepository

class SetDriverAvailabilityUseCase(
    private val repository: DriverHomeRepository,
) {
    suspend operator fun invoke(isAvailable: Boolean): Result<Boolean> = repository.setAvailability(isAvailable)
}
