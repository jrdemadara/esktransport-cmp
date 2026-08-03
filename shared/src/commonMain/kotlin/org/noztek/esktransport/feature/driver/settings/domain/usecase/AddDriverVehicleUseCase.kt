package org.noztek.esktransport.feature.driver.settings.domain.usecase

import org.noztek.esktransport.feature.driver.settings.domain.model.DriverVehicle
import org.noztek.esktransport.feature.driver.settings.domain.model.DriverVehiclePayload
import org.noztek.esktransport.feature.driver.settings.domain.repository.DriverVehicleRepository

class AddDriverVehicleUseCase(
    private val repository: DriverVehicleRepository,
) {
    suspend operator fun invoke(payload: DriverVehiclePayload): Result<DriverVehicle> {
        return repository.addVehicle(payload)
    }
}
