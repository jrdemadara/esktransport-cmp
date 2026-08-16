package org.noztek.esktransport.feature.driver.settings.domain.usecase

import org.noztek.esktransport.feature.driver.settings.domain.repository.DriverVehicleRepository

class GetDriverVehiclePhotoUseCase(
    private val repository: DriverVehicleRepository,
) {
    suspend operator fun invoke(vehiclePublicId: String): Result<ByteArray?> {
        return repository.getVehiclePhoto(vehiclePublicId)
    }
}
