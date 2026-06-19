package org.noztek.esktransport.feature.driver.onboarding.domain.usecase

import org.noztek.esktransport.feature.driver.onboarding.domain.model.DriverServiceZone
import org.noztek.esktransport.feature.driver.onboarding.domain.repository.DriverOnboardingRepository

class GetDriverServiceZonesUseCase(
    private val repository: DriverOnboardingRepository,
) {
    suspend operator fun invoke(): Result<List<DriverServiceZone>> = repository.getServiceZones()
}
