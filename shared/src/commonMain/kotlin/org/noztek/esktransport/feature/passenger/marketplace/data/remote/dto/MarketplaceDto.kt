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
data class MarketplaceServiceRequestResponseDto(
    val message: String? = null,
    val data: MarketplaceServiceRequestDto,
)

@Serializable
data class MarketplaceServiceRequestDto(
    @SerialName("public_id")
    val publicId: String,
    @SerialName("service_type")
    val serviceType: String,
    val status: String,
    @SerialName("workflow_status") val workflowStatus: String? = null,
    @SerialName("requested_start_at")
    val requestedStartAt: String? = null,
    @SerialName("requested_end_at")
    val requestedEndAt: String? = null,
    @SerialName("pickup_label")
    val pickupLabel: String? = null,
    @SerialName("destination_label")
    val destinationLabel: String? = null,
    @SerialName("passenger_count")
    val passengerCount: Int? = null,
    @SerialName("cargo_description")
    val cargoDescription: String? = null,
    @SerialName("cargo_weight_kg")
    val cargoWeightKg: Double? = null,
    @SerialName("cargo_pieces")
    val cargoPieces: Int? = null,
    val currency: String = "PHP",
    @SerialName("quoted_amount") val quotedAmount: Double? = null,
    @SerialName("final_amount") val finalAmount: Double? = null,
    @SerialName("cash_amount") val cashAmount: Double? = null,
    @SerialName("cash_currency") val cashCurrency: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
)

@Serializable
data class CompleteMarketplaceRequestDto(
    @SerialName("cash_amount") val cashAmount: Double,
    @SerialName("cash_currency") val cashCurrency: String,
)

@Serializable
data class CreateMarketplaceServiceRequestDto(
    @SerialName("requested_start_at")
    val requestedStartAt: String? = null,
    @SerialName("requested_end_at")
    val requestedEndAt: String? = null,
    @SerialName("pickup_label")
    val pickupLabel: String? = null,
    @SerialName("destination_label")
    val destinationLabel: String? = null,
    @SerialName("passenger_count")
    val passengerCount: Int? = null,
    @SerialName("cargo_description")
    val cargoDescription: String? = null,
    @SerialName("cargo_weight_kg")
    val cargoWeightKg: Double? = null,
    @SerialName("cargo_pieces")
    val cargoPieces: Int? = null,
    @SerialName("customer_note")
    val customerNote: String? = null,
)

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
