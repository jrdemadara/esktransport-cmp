package org.noztek.esktransport.feature.driver.onboarding.domain.usecase

import org.noztek.esktransport.feature.driver.onboarding.domain.model.DriverOnboardingStatus
import org.noztek.esktransport.feature.driver.onboarding.domain.repository.DriverOnboardingRepository

class GetDriverOnboardingStatusUseCase(
    private val repository: DriverOnboardingRepository,
) {
    suspend operator fun invoke(): Result<DriverOnboardingStatus> = repository.getStatus()
}
