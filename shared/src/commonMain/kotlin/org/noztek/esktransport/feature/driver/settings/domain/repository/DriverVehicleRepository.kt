package org.noztek.esktransport.feature.driver.settings.domain.repository

import org.noztek.esktransport.feature.driver.settings.domain.model.DriverVehicle
import org.noztek.esktransport.feature.driver.settings.domain.model.DriverVehicleDocumentUploadPayload
import org.noztek.esktransport.feature.driver.settings.domain.model.DriverVehiclePayload
import org.noztek.esktransport.feature.driver.settings.domain.model.DriverVehicleServicesPayload
import org.noztek.esktransport.feature.driver.settings.domain.model.DriverVehicleType

interface DriverVehicleRepository {
    suspend fun getVehicleTypes(): Result<List<DriverVehicleType>>

    suspend fun getVehicles(): Result<List<DriverVehicle>>

    suspend fun getVehicle(vehiclePublicId: String): Result<DriverVehicle>

    suspend fun getVehiclePhoto(vehiclePublicId: String): Result<ByteArray?>

    suspend fun addVehicle(payload: DriverVehiclePayload): Result<DriverVehicle>

    suspend fun updateVehicle(vehiclePublicId: String, payload: DriverVehiclePayload): Result<DriverVehicle>

    suspend fun updateVehicleServices(
        vehiclePublicId: String,
        payload: DriverVehicleServicesPayload,
    ): Result<DriverVehicle>

    suspend fun uploadVehicleDocument(
        vehiclePublicId: String,
        payload: DriverVehicleDocumentUploadPayload,
    ): Result<DriverVehicle>

    suspend fun activateRideVehicle(vehiclePublicId: String): Result<DriverVehicle>
}
