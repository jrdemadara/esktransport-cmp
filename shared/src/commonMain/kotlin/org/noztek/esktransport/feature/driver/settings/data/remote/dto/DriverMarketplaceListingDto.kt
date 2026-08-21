package org.noztek.esktransport.feature.driver.settings.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.noztek.esktransport.feature.driver.onboarding.domain.model.toDriverRequirementStatus
import org.noztek.esktransport.feature.driver.onboarding.domain.model.toDriverVehicleServiceType
import org.noztek.esktransport.feature.driver.settings.domain.model.DriverMarketplaceListing
import org.noztek.esktransport.feature.driver.settings.domain.model.DriverMarketplaceListingPayload
import org.noztek.esktransport.feature.driver.settings.domain.model.DriverMarketplaceListingVehicle

@Serializable
data class DriverMarketplaceListingsResponseDto(
    val data: List<DriverMarketplaceListingDto> = emptyList(),
)

@Serializable
data class DriverMarketplaceListingResponseDto(
    val message: String? = null,
    val data: DriverMarketplaceListingDto,
)

@Serializable
data class DriverMarketplaceListingDto(
    @SerialName("public_id")
    val publicId: String,
    @SerialName("service_type")
    val serviceType: String,
    val title: String,
    val description: String? = null,
    @SerialName("base_rate")
    val baseRate: Double? = null,
    @SerialName("rate_unit")
    val rateUnit: String? = null,
    @SerialName("minimum_hours")
    val minimumHours: Double? = null,
    @SerialName("included_km")
    val includedKm: Double? = null,
    val currency: String = "PHP",
    val status: String,
    @SerialName("service_status")
    val serviceStatus: String,
    @SerialName("service_enabled")
    val serviceEnabled: Boolean,
    @SerialName("service_rejection_reason")
    val serviceRejectionReason: String? = null,
    val vehicle: DriverMarketplaceListingVehicleDto,
    @SerialName("updated_at")
    val updatedAt: String? = null,
)

@Serializable
data class DriverMarketplaceListingVehicleDto(
    @SerialName("public_id")
    val publicId: String? = null,
    val plate: String,
    val make: String? = null,
    val model: String? = null,
    val year: Int? = null,
    @SerialName("vehicle_type_code")
    val vehicleTypeCode: String? = null,
    @SerialName("vehicle_type_label")
    val vehicleTypeLabel: String? = null,
    @SerialName("passenger_capacity")
    val passengerCapacity: Int? = null,
    @SerialName("payload_kg")
    val payloadKg: Double? = null,
    @SerialName("volume_m3")
    val volumeM3: Double? = null,
)

@Serializable
data class DriverMarketplaceListingRequestDto(
    val title: String,
    val description: String? = null,
    @SerialName("base_rate")
    val baseRate: Double? = null,
    @SerialName("rate_unit")
    val rateUnit: String? = null,
    @SerialName("minimum_hours")
    val minimumHours: Double? = null,
    @SerialName("included_km")
    val includedKm: Double? = null,
    val currency: String = "PHP",
    val status: String? = null,
)

fun DriverMarketplaceListingDto.toDomain(): DriverMarketplaceListing {
    return DriverMarketplaceListing(
        publicId = publicId,
        serviceType = serviceType.toDriverVehicleServiceType(),
        title = title,
        description = description,
        baseRate = baseRate,
        rateUnit = rateUnit,
        minimumHours = minimumHours,
        includedKm = includedKm,
        currency = currency,
        status = status,
        serviceStatus = serviceStatus.toDriverRequirementStatus(),
        serviceEnabled = serviceEnabled,
        serviceRejectionReason = serviceRejectionReason,
        vehicle = vehicle.toDomain(),
        updatedAt = updatedAt,
    )
}

fun DriverMarketplaceListingPayload.toRequestDto(): DriverMarketplaceListingRequestDto {
    return DriverMarketplaceListingRequestDto(
        title = title,
        description = description,
        baseRate = baseRate,
        rateUnit = rateUnit,
        minimumHours = minimumHours,
        includedKm = includedKm,
        currency = currency,
        status = status,
    )
}

private fun DriverMarketplaceListingVehicleDto.toDomain(): DriverMarketplaceListingVehicle {
    return DriverMarketplaceListingVehicle(
        publicId = publicId,
        plate = plate,
        make = make,
        model = model,
        year = year,
        vehicleTypeCode = vehicleTypeCode,
        vehicleTypeLabel = vehicleTypeLabel,
        passengerCapacity = passengerCapacity,
        payloadKg = payloadKg,
        volumeM3 = volumeM3,
    )
}
