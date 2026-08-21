package org.noztek.esktransport.feature.passenger.marketplace.domain.model

data class MarketplaceVehicleType(
    val code: String,
    val name: String,
    val description: String?,
    val supportsCargo: Boolean,
    val passengerMax: Int?,
    val sortOrder: Int,
    val allowedServices: List<String>,
)

data class MarketplaceListing(
    val publicId: String,
    val serviceType: String,
    val title: String,
    val description: String?,
    val baseRate: Double?,
    val rateUnit: String?,
    val minimumHours: Double?,
    val includedKm: Double?,
    val currency: String,
    val owner: MarketplaceListingOwner,
    val vehicle: MarketplaceListingVehicle,
    val updatedAt: String?,
)

data class MarketplaceListingOwner(
    val name: String,
)

data class MarketplaceListingVehicle(
    val id: Long,
    val vehicleTypeCode: String,
    val vehicleTypeLabel: String?,
    val plate: String,
    val make: String?,
    val model: String?,
    val year: Int?,
    val payloadKg: Double?,
    val volumeM3: Double?,
    val passengerCapacity: Int?,
)
