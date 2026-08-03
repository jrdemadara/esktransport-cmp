package org.noztek.esktransport.feature.driver.settings.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.noztek.esktransport.feature.driver.onboarding.domain.model.toDriverRequirementStatus
import org.noztek.esktransport.feature.driver.onboarding.domain.model.toDriverVehicleServiceType
import org.noztek.esktransport.feature.driver.settings.domain.model.DriverVehicle
import org.noztek.esktransport.feature.driver.settings.domain.model.DriverVehicleDocument
import org.noztek.esktransport.feature.driver.settings.domain.model.DriverVehiclePayload
import org.noztek.esktransport.feature.driver.settings.domain.model.DriverVehicleServiceStatus
import org.noztek.esktransport.feature.driver.settings.domain.model.DriverVehicleServicesPayload
import org.noztek.esktransport.feature.driver.settings.domain.model.DriverVehicleType

@Serializable
data class DriverVehiclesResponseDto(
    val data: List<DriverVehicleDto>,
)

@Serializable
data class DriverVehicleResponseDto(
    val data: DriverVehicleDto,
)

@Serializable
data class DriverVehicleTypesResponseDto(
    val data: List<DriverVehicleTypeDto>,
)

@Serializable
data class DriverVehicleTypeDto(
    val code: String,
    val name: String,
    val description: String? = null,
    @SerialName("supports_cargo")
    val supportsCargo: Boolean = false,
    @SerialName("passenger_max")
    val passengerMax: Int? = null,
    @SerialName("sort_order")
    val sortOrder: Int = 100,
    @SerialName("allowed_services")
    val allowedServices: List<String> = emptyList(),
)

@Serializable
data class DriverVehicleDto(
    @SerialName("public_id")
    val publicId: String,
    @SerialName("vehicle_type_code")
    val vehicleTypeCode: String? = null,
    val plate: String,
    val make: String? = null,
    val model: String? = null,
    val year: Int? = null,
    @SerialName("payload_kg")
    val payloadKg: Double? = null,
    @SerialName("volume_m3")
    val volumeM3: Double? = null,
    @SerialName("passenger_capacity")
    val passengerCapacity: Int? = null,
    val status: String,
    @SerialName("is_available")
    val isAvailable: Boolean,
    @SerialName("is_active_ride_vehicle")
    val isActiveRideVehicle: Boolean,
    @SerialName("verification_status")
    val verificationStatus: String,
    val documents: List<DriverVehicleDocumentDto> = emptyList(),
    val services: List<DriverVehicleServiceDto> = emptyList(),
)

@Serializable
data class DriverVehicleDocumentDto(
    val type: String,
    val status: String,
    @SerialName("rejection_reason")
    val rejectionReason: String? = null,
    @SerialName("reviewed_at")
    val reviewedAt: String? = null,
    @SerialName("expires_at")
    val expiresAt: String? = null,
)

@Serializable
data class DriverVehicleServiceDto(
    @SerialName("service_type")
    val serviceType: String,
    val status: String,
    @SerialName("is_enabled")
    val isEnabled: Boolean,
    @SerialName("rejection_reason")
    val rejectionReason: String? = null,
    @SerialName("reviewed_at")
    val reviewedAt: String? = null,
)

@Serializable
data class DriverVehicleRequestDto(
    @SerialName("vehicle_type_code")
    val vehicleTypeCode: String,
    val plate: String,
    val make: String? = null,
    val model: String? = null,
    val year: Int? = null,
    @SerialName("payload_kg")
    val payloadKg: Double? = null,
    @SerialName("volume_m3")
    val volumeM3: Double? = null,
    @SerialName("passenger_capacity")
    val passengerCapacity: Int? = null,
    val services: List<String> = emptyList(),
)

@Serializable
data class DriverVehicleServicesRequestDto(
    val services: List<String>,
)

fun DriverVehicleDto.toDomain(): DriverVehicle {
    return DriverVehicle(
        publicId = publicId,
        vehicleTypeCode = vehicleTypeCode,
        plate = plate,
        make = make,
        model = model,
        year = year,
        payloadKg = payloadKg,
        volumeM3 = volumeM3,
        passengerCapacity = passengerCapacity,
        status = status,
        isAvailable = isAvailable,
        isActiveRideVehicle = isActiveRideVehicle,
        verificationStatus = verificationStatus.toDriverRequirementStatus(),
        documents = documents.map { it.toDomain() },
        services = services.map { it.toDomain() },
    )
}

fun DriverVehicleTypeDto.toDomain(): DriverVehicleType {
    return DriverVehicleType(
        code = code,
        name = name,
        description = description,
        supportsCargo = supportsCargo,
        passengerMax = passengerMax,
        sortOrder = sortOrder,
        allowedServices = allowedServices.map { it.toDriverVehicleServiceType() },
    )
}

private fun DriverVehicleDocumentDto.toDomain(): DriverVehicleDocument {
    return DriverVehicleDocument(
        type = type,
        status = status.toDriverRequirementStatus(),
        rejectionReason = rejectionReason,
        reviewedAt = reviewedAt,
        expiresAt = expiresAt,
    )
}

private fun DriverVehicleServiceDto.toDomain(): DriverVehicleServiceStatus {
    return DriverVehicleServiceStatus(
        serviceType = serviceType.toDriverVehicleServiceType(),
        status = status.toDriverRequirementStatus(),
        isEnabled = isEnabled,
        rejectionReason = rejectionReason,
        reviewedAt = reviewedAt,
    )
}

fun DriverVehiclePayload.toRequestDto(): DriverVehicleRequestDto {
    return DriverVehicleRequestDto(
        vehicleTypeCode = vehicleTypeCode,
        plate = plate,
        make = make,
        model = model,
        year = year,
        payloadKg = payloadKg,
        volumeM3 = volumeM3,
        passengerCapacity = passengerCapacity,
        services = services.map { it.apiValue },
    )
}

fun DriverVehicleServicesPayload.toRequestDto(): DriverVehicleServicesRequestDto {
    return DriverVehicleServicesRequestDto(
        services = services.map { it.apiValue },
    )
}
