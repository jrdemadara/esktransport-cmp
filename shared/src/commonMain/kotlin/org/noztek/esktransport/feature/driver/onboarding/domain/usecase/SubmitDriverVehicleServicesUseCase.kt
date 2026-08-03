package org.noztek.esktransport.feature.driver.onboarding.domain.usecase

import org.noztek.esktransport.feature.driver.onboarding.domain.model.DriverOnboardingStatus
import org.noztek.esktransport.feature.driver.onboarding.domain.model.DriverVehicleServiceSelectionPayload
import org.noztek.esktransport.feature.driver.onboarding.domain.repository.DriverOnboardingRepository

class SubmitDriverVehicleServicesUseCase(
    private val repository: DriverOnboardingRepository,
) {
    suspend operator fun invoke(payload: DriverVehicleServiceSelectionPayload): Result<DriverOnboardingStatus> {
        return repository.submitVehicleServices(payload)
    }
}
