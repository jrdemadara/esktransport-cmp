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

        val registrationResult = repository.uploadDocument(payload.registrationDocument)
        val registrationFailure = registrationResult.exceptionOrNull()
        if (registrationFailure != null) return Result.failure(registrationFailure)

        return repository.uploadDocument(payload.vehiclePhoto)
    }
}
