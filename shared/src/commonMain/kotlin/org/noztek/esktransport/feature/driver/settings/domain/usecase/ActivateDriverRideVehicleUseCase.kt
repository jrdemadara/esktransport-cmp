package org.noztek.esktransport.feature.driver.settings.domain.usecase

import org.noztek.esktransport.feature.driver.settings.domain.model.DriverVehicle
import org.noztek.esktransport.feature.driver.settings.domain.repository.DriverVehicleRepository

class ActivateDriverRideVehicleUseCase(
    private val repository: DriverVehicleRepository,
) {
    suspend operator fun invoke(vehiclePublicId: String): Result<DriverVehicle> {
        return repository.activateRideVehicle(vehiclePublicId)
    }
}
