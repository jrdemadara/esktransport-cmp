package org.noztek.esktransport.feature.driver.onboarding.domain.usecase

import org.noztek.esktransport.feature.driver.onboarding.domain.model.DriverOnboardingStatus
import org.noztek.esktransport.feature.driver.onboarding.domain.model.DriverServiceZoneSelectionPayload
import org.noztek.esktransport.feature.driver.onboarding.domain.repository.DriverOnboardingRepository

class SubmitDriverServiceZonesUseCase(
    private val repository: DriverOnboardingRepository,
) {
    suspend operator fun invoke(payload: DriverServiceZoneSelectionPayload): Result<DriverOnboardingStatus> {
        return repository.submitServiceZones(payload)
    }
}
