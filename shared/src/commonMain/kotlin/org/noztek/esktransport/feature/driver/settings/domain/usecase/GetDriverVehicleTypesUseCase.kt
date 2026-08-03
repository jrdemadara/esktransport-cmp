package org.noztek.esktransport.feature.driver.settings.domain.usecase

import org.noztek.esktransport.feature.driver.settings.domain.model.DriverVehicleType
import org.noztek.esktransport.feature.driver.settings.domain.repository.DriverVehicleRepository

class GetDriverVehicleTypesUseCase(
    private val repository: DriverVehicleRepository,
) {
    suspend operator fun invoke(): Result<List<DriverVehicleType>> {
        return repository.getVehicleTypes()
    }
}
