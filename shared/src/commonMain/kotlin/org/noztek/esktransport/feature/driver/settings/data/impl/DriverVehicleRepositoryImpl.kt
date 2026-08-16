package org.noztek.esktransport.feature.driver.settings.data.impl

import org.noztek.esktransport.core.network.ApiErrorParser
import org.noztek.esktransport.feature.driver.settings.data.remote.DriverVehicleApi
import org.noztek.esktransport.feature.driver.settings.data.remote.dto.toDomain
import org.noztek.esktransport.feature.driver.settings.data.remote.dto.toRequestDto
import org.noztek.esktransport.feature.driver.settings.domain.model.DriverVehicle
import org.noztek.esktransport.feature.driver.settings.domain.model.DriverVehicleDocumentUploadPayload
import org.noztek.esktransport.feature.driver.settings.domain.model.DriverVehiclePayload
import org.noztek.esktransport.feature.driver.settings.domain.model.DriverVehicleServicesPayload
import org.noztek.esktransport.feature.driver.settings.domain.model.DriverVehicleType
import org.noztek.esktransport.feature.driver.settings.domain.repository.DriverVehicleRepository

class DriverVehicleRepositoryImpl(
    private val api: DriverVehicleApi,
) : DriverVehicleRepository {
    override suspend fun getVehicleTypes(): Result<List<DriverVehicleType>> {
        return try {
            Result.success(api.getVehicleTypes().data.map { it.toDomain() })
        } catch (throwable: Throwable) {
            val message = ApiErrorParser.parse(throwable, "Failed to load vehicle types.")
            Result.failure(IllegalStateException(message))
        }
    }

    override suspend fun getVehicles(): Result<List<DriverVehicle>> {
        return try {
            Result.success(api.getVehicles().data.map { it.toDomain() })
        } catch (throwable: Throwable) {
            val message = ApiErrorParser.parse(throwable, "Failed to load vehicles.")
            Result.failure(IllegalStateException(message))
        }
    }

    override suspend fun getVehicle(vehiclePublicId: String): Result<DriverVehicle> {
        return try {
            Result.success(api.getVehicle(vehiclePublicId).data.toDomain())
        } catch (throwable: Throwable) {
            val message = ApiErrorParser.parse(throwable, "Failed to load vehicle.")
            Result.failure(IllegalStateException(message))
        }
    }

    override suspend fun getVehiclePhoto(vehiclePublicId: String): Result<ByteArray?> {
        return try {
            Result.success(api.getVehiclePhoto(vehiclePublicId))
        } catch (throwable: Throwable) {
            val message = ApiErrorParser.parse(throwable, "Failed to load vehicle photo.")
            Result.failure(IllegalStateException(message))
        }
    }

    override suspend fun addVehicle(payload: DriverVehiclePayload): Result<DriverVehicle> {
        return try {
            Result.success(api.addVehicle(payload.toRequestDto()).data.toDomain())
        } catch (throwable: Throwable) {
            val message = ApiErrorParser.parse(throwable, "Failed to add vehicle.")
            Result.failure(IllegalStateException(message))
        }
    }

    override suspend fun updateVehicle(vehiclePublicId: String, payload: DriverVehiclePayload): Result<DriverVehicle> {
        return try {
            Result.success(api.updateVehicle(vehiclePublicId, payload.toRequestDto()).data.toDomain())
        } catch (throwable: Throwable) {
            val message = ApiErrorParser.parse(throwable, "Failed to update vehicle.")
            Result.failure(IllegalStateException(message))
        }
    }

    override suspend fun updateVehicleServices(
        vehiclePublicId: String,
        payload: DriverVehicleServicesPayload,
    ): Result<DriverVehicle> {
        return try {
            Result.success(api.updateVehicleServices(vehiclePublicId, payload.toRequestDto()).data.toDomain())
        } catch (throwable: Throwable) {
            val message = ApiErrorParser.parse(throwable, "Failed to save vehicle services.")
            Result.failure(IllegalStateException(message))
        }
    }

    override suspend fun uploadVehicleDocument(
        vehiclePublicId: String,
        payload: DriverVehicleDocumentUploadPayload,
    ): Result<DriverVehicle> {
        return try {
            Result.success(api.uploadVehicleDocument(vehiclePublicId, payload).data.toDomain())
        } catch (throwable: Throwable) {
            val message = ApiErrorParser.parse(throwable, "Failed to upload vehicle document.")
            Result.failure(IllegalStateException(message))
        }
    }

    override suspend fun activateRideVehicle(vehiclePublicId: String): Result<DriverVehicle> {
        return try {
            Result.success(api.activateRideVehicle(vehiclePublicId).data.toDomain())
        } catch (throwable: Throwable) {
            val message = ApiErrorParser.parse(throwable, "Failed to set active City Ride vehicle.")
            Result.failure(IllegalStateException(message))
        }
    }
}
