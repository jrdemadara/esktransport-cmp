package org.noztek.esktransport.feature.driver.settings.domain.usecase

import org.noztek.esktransport.feature.driver.settings.domain.model.DriverVehicle
import org.noztek.esktransport.feature.driver.settings.domain.repository.DriverVehicleRepository

class GetDriverVehiclesUseCase(
    private val repository: DriverVehicleRepository,
) {
    suspend operator fun invoke(): Result<List<DriverVehicle>> = repository.getVehicles()
}
