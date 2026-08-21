package org.noztek.esktransport.feature.passenger.marketplace.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VehicleTypeLookupResponseDto(
    val data: List<MarketplaceVehicleTypeDto> = emptyList(),
)

@Serializable
data class MarketplaceVehicleTypeDto(
    val code: String,
    val name: String,
    val description: String? = null,
    @SerialName("supports_cargo")
    val supportsCargo: Boolean = false,
    @SerialName("passenger_max")
    val passengerMax: Int? = null,
    @SerialName("sort_order")
    val sortOrder: Int = 0,
    @SerialName("allowed_services")
    val allowedServices: List<String> = emptyList(),
)

@Serializable
data class MarketplaceListingsResponseDto(
    val data: List<MarketplaceListingDto> = emptyList(),
) {
    @Serializable
    data class Single(
        val data: MarketplaceListingDto,
    )
}

@Serializable
data class MarketplaceListingDto(
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
    val owner: MarketplaceOwnerDto,
    val vehicle: MarketplaceVehicleDto,
    @SerialName("updated_at")
    val updatedAt: String? = null,
)

@Serializable
data class MarketplaceOwnerDto(
    val name: String,
)

@Serializable
data class MarketplaceVehicleDto(
    val id: Long,
    @SerialName("vehicle_type_code")
    val vehicleTypeCode: String,
    @SerialName("vehicle_type_label")
    val vehicleTypeLabel: String? = null,
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
)
