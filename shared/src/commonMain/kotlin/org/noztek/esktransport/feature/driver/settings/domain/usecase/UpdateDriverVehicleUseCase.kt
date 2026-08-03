package org.noztek.esktransport.feature.driver.settings.domain.usecase

import org.noztek.esktransport.feature.driver.settings.domain.model.DriverVehicle
import org.noztek.esktransport.feature.driver.settings.domain.model.DriverVehiclePayload
import org.noztek.esktransport.feature.driver.settings.domain.repository.DriverVehicleRepository

class UpdateDriverVehicleUseCase(
    private val repository: DriverVehicleRepository,
) {
    suspend operator fun invoke(vehiclePublicId: String, payload: DriverVehiclePayload): Result<DriverVehicle> {
        return repository.updateVehicle(vehiclePublicId, payload)
    }
}
