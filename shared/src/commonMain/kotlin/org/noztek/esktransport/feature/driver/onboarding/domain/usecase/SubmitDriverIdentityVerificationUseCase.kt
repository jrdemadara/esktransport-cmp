package org.noztek.esktransport.feature.driver.onboarding.domain.usecase

import org.noztek.esktransport.feature.driver.onboarding.domain.model.DriverIdentityVerificationPayload
import org.noztek.esktransport.feature.driver.onboarding.domain.model.DriverOnboardingStatus
import org.noztek.esktransport.feature.driver.onboarding.domain.repository.DriverOnboardingRepository

class SubmitDriverIdentityVerificationUseCase(
    private val repository: DriverOnboardingRepository,
) {
    suspend operator fun invoke(payload: DriverIdentityVerificationPayload): Result<DriverOnboardingStatus> {
        return repository.submitIdentityVerification(payload)
    }
}
