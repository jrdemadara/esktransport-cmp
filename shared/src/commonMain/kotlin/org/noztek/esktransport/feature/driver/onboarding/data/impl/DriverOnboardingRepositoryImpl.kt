package org.noztek.esktransport.feature.driver.onboarding.data.impl

import org.noztek.esktransport.core.network.ApiErrorParser
import org.noztek.esktransport.feature.driver.onboarding.data.remote.DriverOnboardingApi
import org.noztek.esktransport.feature.driver.onboarding.data.remote.dto.DriverVehicleSetupRequestDto
import org.noztek.esktransport.feature.driver.onboarding.domain.model.DriverOnboardingDocumentUpload
import org.noztek.esktransport.feature.driver.onboarding.domain.model.DriverOnboardingStatus
import org.noztek.esktransport.feature.driver.onboarding.domain.model.DriverVehicleSetupPayload
import org.noztek.esktransport.feature.driver.onboarding.domain.repository.DriverOnboardingRepository

class DriverOnboardingRepositoryImpl(
    private val api: DriverOnboardingApi,
) : DriverOnboardingRepository {
    override suspend fun getStatus(): Result<DriverOnboardingStatus> {
        return try {
            Result.success(api.getStatus().data.toDomain())
        } catch (throwable: Throwable) {
            val message = ApiErrorParser.parse(throwable, "Failed to load driver setup.")
            Result.failure(IllegalStateException(message))
        }
    }

    override suspend fun saveVehicle(payload: DriverVehicleSetupPayload): Result<DriverOnboardingStatus> {
        return try {
            val response = api.saveVehicle(
                DriverVehicleSetupRequestDto(
                    vehicleTypeCode = payload.vehicleTypeCode,
                    plate = payload.plate,
                    make = payload.make,
                    model = payload.model,
                    year = payload.year,
                    passengerCapacity = payload.passengerCapacity,
                ),
            )
            Result.success(response.data.toDomain())
        } catch (throwable: Throwable) {
            val message = ApiErrorParser.parse(throwable, "Failed to save vehicle setup.")
            Result.failure(IllegalStateException(message))
        }
    }

    override suspend fun uploadDocument(upload: DriverOnboardingDocumentUpload): Result<DriverOnboardingStatus> {
        return try {
            Result.success(api.uploadDocument(upload).data.toDomain())
        } catch (throwable: Throwable) {
            val message = ApiErrorParser.parse(throwable, "Failed to upload document.")
            Result.failure(IllegalStateException(message))
        }
    }

    override suspend fun submitForReview(): Result<DriverOnboardingStatus> {
        return try {
            Result.success(api.submitForReview().data.toDomain())
        } catch (throwable: Throwable) {
            val message = ApiErrorParser.parse(throwable, "Failed to submit driver setup.")
            Result.failure(IllegalStateException(message))
        }
    }
}
