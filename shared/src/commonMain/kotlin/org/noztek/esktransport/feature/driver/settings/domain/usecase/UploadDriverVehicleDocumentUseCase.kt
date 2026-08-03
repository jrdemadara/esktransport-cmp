package org.noztek.esktransport.feature.driver.settings.domain.usecase

import org.noztek.esktransport.feature.driver.settings.domain.model.DriverVehicle
import org.noztek.esktransport.feature.driver.settings.domain.model.DriverVehicleDocumentUploadPayload
import org.noztek.esktransport.feature.driver.settings.domain.repository.DriverVehicleRepository

class UploadDriverVehicleDocumentUseCase(
    private val repository: DriverVehicleRepository,
) {
    suspend operator fun invoke(
        vehiclePublicId: String,
        payload: DriverVehicleDocumentUploadPayload,
    ): Result<DriverVehicle> {
        return repository.uploadVehicleDocument(vehiclePublicId, payload)
    }
}
