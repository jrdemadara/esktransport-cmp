package org.noztek.esktransport.feature.driver.go.domain.usecase

import org.noztek.esktransport.feature.driver.go.domain.repository.GoRepository

class GetDriverAvailabilityUseCase(
    private val repository: GoRepository,
) {
    suspend operator fun invoke(): Result<Boolean> = repository.getAvailability()
}

