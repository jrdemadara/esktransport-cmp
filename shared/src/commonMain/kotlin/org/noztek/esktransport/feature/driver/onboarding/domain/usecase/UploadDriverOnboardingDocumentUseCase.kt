package org.noztek.esktransport.feature.driver.onboarding.domain.usecase

import org.noztek.esktransport.feature.driver.onboarding.domain.model.DriverOnboardingDocumentUpload
import org.noztek.esktransport.feature.driver.onboarding.domain.model.DriverOnboardingStatus
import org.noztek.esktransport.feature.driver.onboarding.domain.repository.DriverOnboardingRepository

class UploadDriverOnboardingDocumentUseCase(
    private val repository: DriverOnboardingRepository,
) {
    suspend operator fun invoke(upload: DriverOnboardingDocumentUpload): Result<DriverOnboardingStatus> {
        return repository.uploadDocument(upload)
    }
}
