package org.noztek.esktransport.feature.driver.settings.domain.model

import org.noztek.esktransport.feature.driver.onboarding.domain.model.DriverRequirementStatus
import org.noztek.esktransport.feature.driver.onboarding.domain.model.DriverVehicleServiceType

data class DriverMarketplaceListing(
    val publicId: String,
    val serviceType: DriverVehicleServiceType,
    val title: String,
    val description: String?,
    val baseRate: Double?,
    val rateUnit: String?,
    val minimumHours: Double?,
    val includedKm: Double?,
    val currency: String,
    val status: String,
    val serviceStatus: DriverRequirementStatus,
    val serviceEnabled: Boolean,
    val serviceRejectionReason: String?,
    val vehicle: DriverMarketplaceListingVehicle,
    val updatedAt: String?,
)

data class DriverMarketplaceListingVehicle(
    val publicId: String?,
    val plate: String,
    val make: String?,
    val model: String?,
    val year: Int?,
    val vehicleTypeCode: String?,
    val vehicleTypeLabel: String?,
    val passengerCapacity: Int?,
    val payloadKg: Double?,
    val volumeM3: Double?,
)

data class DriverMarketplaceListingPayload(
    val title: String,
    val description: String?,
    val baseRate: Double?,
    val rateUnit: String?,
    val minimumHours: Double?,
    val includedKm: Double?,
    val currency: String,
    val status: String?,
)
