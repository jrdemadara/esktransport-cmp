package org.noztek.esktransport.feature.driver.onboarding.data.impl

import io.ktor.client.plugins.ResponseException
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.noztek.esktransport.core.network.ApiErrorParser
import org.noztek.esktransport.feature.driver.onboarding.data.remote.DriverOnboardingApi
import org.noztek.esktransport.feature.driver.onboarding.data.remote.dto.DriverServiceZoneSelectionRequestDto
import org.noztek.esktransport.feature.driver.onboarding.data.remote.dto.DriverVehicleSetupRequestDto
import org.noztek.esktransport.feature.driver.onboarding.domain.model.DriverIdentityVerificationPayload
import org.noztek.esktransport.feature.driver.onboarding.domain.model.DriverOnboardingDocumentUpload
import org.noztek.esktransport.feature.driver.onboarding.domain.model.DriverOnboardingStatus
import org.noztek.esktransport.feature.driver.onboarding.domain.model.DriverServiceZone
import org.noztek.esktransport.feature.driver.onboarding.domain.model.DriverServiceZoneSelectionPayload
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

    override suspend fun submitIdentityVerification(payload: DriverIdentityVerificationPayload): Result<DriverOnboardingStatus> {
        return try {
            Result.success(api.submitIdentityVerification(payload).data.toDomain())
        } catch (throwable: Throwable) {
            val message = parseUploadError(throwable, "Failed to submit identity verification.")
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

    override suspend fun getServiceZones(): Result<List<DriverServiceZone>> {
        return try {
            Result.success(api.getServiceZones().data.map { it.toDomain() })
        } catch (throwable: Throwable) {
            val message = ApiErrorParser.parse(throwable, "Failed to load service zones.")
            Result.failure(IllegalStateException(message))
        }
    }

    override suspend fun submitServiceZones(payload: DriverServiceZoneSelectionPayload): Result<DriverOnboardingStatus> {
        return try {
            val response = api.submitServiceZones(
                DriverServiceZoneSelectionRequestDto(serviceZoneIds = payload.serviceZoneIds),
            )
            Result.success(response.data.toDomain())
        } catch (throwable: Throwable) {
            val message = ApiErrorParser.parse(throwable, "Failed to save service zones.")
            Result.failure(IllegalStateException(message))
        }
    }

    override suspend fun uploadDocument(upload: DriverOnboardingDocumentUpload): Result<DriverOnboardingStatus> {
        return try {
            Result.success(api.uploadDocument(upload).data.toDomain())
        } catch (throwable: Throwable) {
            val message = parseUploadError(throwable, "Failed to upload document.")
            Result.failure(IllegalStateException(message))
        }
    }

}

private suspend fun parseUploadError(throwable: Throwable, fallback: String): String {
    if (throwable is ResponseException) {
        val body = runCatching { throwable.response.bodyAsText() }.getOrNull()
        val parsed = body?.firstLaravelValidationMessage()
        if (!parsed.isNullOrBlank()) return parsed
    }

    return ApiErrorParser.parse(throwable, fallback)
}

private fun String.firstLaravelValidationMessage(): String? {
    val root = runCatching { Json.parseToJsonElement(this).jsonObject }.getOrNull() ?: return null
    val errors = root["errors"] as? JsonObject
    val firstFieldError = errors
        ?.values
        ?.firstOrNull()
        ?.let { value ->
            when (value) {
                is JsonArray -> value.firstOrNull()?.jsonPrimitive?.content
                else -> value.jsonPrimitive.content
            }
        }

    return firstFieldError ?: root["message"]?.jsonPrimitive?.content
}
