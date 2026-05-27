package org.noztek.esktransport.feature.driver.home.domain.usecase

import org.noztek.esktransport.feature.driver.home.domain.repository.DriverHomeRepository

class GetDriverAvailabilityUseCase(
    private val repository: DriverHomeRepository,
) {
    suspend operator fun invoke(): Result<Boolean> = repository.getAvailability()
}

