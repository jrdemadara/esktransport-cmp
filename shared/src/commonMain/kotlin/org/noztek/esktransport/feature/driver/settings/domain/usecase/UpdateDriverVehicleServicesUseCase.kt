package org.noztek.esktransport.feature.driver.settings.domain.usecase

import org.noztek.esktransport.feature.driver.settings.domain.model.DriverVehicle
import org.noztek.esktransport.feature.driver.settings.domain.model.DriverVehicleServicesPayload
import org.noztek.esktransport.feature.driver.settings.domain.repository.DriverVehicleRepository

class UpdateDriverVehicleServicesUseCase(
    private val repository: DriverVehicleRepository,
) {
    suspend operator fun invoke(
        vehiclePublicId: String,
        payload: DriverVehicleServicesPayload,
    ): Result<DriverVehicle> {
        return repository.updateVehicleServices(vehiclePublicId, payload)
    }
}
