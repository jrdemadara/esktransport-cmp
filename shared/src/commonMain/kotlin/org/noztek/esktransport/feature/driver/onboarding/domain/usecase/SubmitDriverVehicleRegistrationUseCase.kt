package org.noztek.esktransport.feature.driver.onboarding.domain.usecase

import org.noztek.esktransport.feature.driver.onboarding.domain.model.DriverOnboardingStatus
import org.noztek.esktransport.feature.driver.onboarding.domain.model.DriverVehicleRegistrationPayload
import org.noztek.esktransport.feature.driver.onboarding.domain.repository.DriverOnboardingRepository

class SubmitDriverVehicleRegistrationUseCase(
    private val repository: DriverOnboardingRepository,
) {
    suspend operator fun invoke(payload: DriverVehicleRegistrationPayload): Result<DriverOnboardingStatus> {
        val vehicleResult = repository.saveVehicle(payload.vehicle)
        val vehicleFailure = vehicleResult.exceptionOrNull()
        if (vehicleFailure != null) return Result.failure(vehicleFailure)

        return repository.uploadDocument(payload.registrationDocument)
    }
}
