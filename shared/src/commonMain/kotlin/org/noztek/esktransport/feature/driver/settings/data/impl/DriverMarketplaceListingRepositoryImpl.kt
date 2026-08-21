package org.noztek.esktransport.feature.driver.settings.data.impl

import org.noztek.esktransport.core.network.ApiErrorParser
import org.noztek.esktransport.feature.driver.onboarding.domain.model.DriverVehicleServiceType
import org.noztek.esktransport.feature.driver.settings.data.remote.DriverMarketplaceListingApi
import org.noztek.esktransport.feature.driver.settings.data.remote.dto.toDomain
import org.noztek.esktransport.feature.driver.settings.data.remote.dto.toRequestDto
import org.noztek.esktransport.feature.driver.settings.domain.model.DriverMarketplaceListing
import org.noztek.esktransport.feature.driver.settings.domain.model.DriverMarketplaceListingPayload
import org.noztek.esktransport.feature.driver.settings.domain.repository.DriverMarketplaceListingRepository

class DriverMarketplaceListingRepositoryImpl(
    private val api: DriverMarketplaceListingApi,
) : DriverMarketplaceListingRepository {
    override suspend fun getListingForVehicle(
        vehiclePublicId: String,
        serviceType: DriverVehicleServiceType,
    ): Result<DriverMarketplaceListing> {
        return try {
            val listing = api.getListings()
                .data
                .map { it.toDomain() }
                .firstOrNull { listing ->
                    listing.vehicle.publicId == vehiclePublicId &&
                        listing.serviceType == serviceType
                }

            if (listing == null) {
                Result.failure(IllegalStateException("Listing not found for this vehicle."))
            } else {
                Result.success(listing)
            }
        } catch (throwable: Throwable) {
            val message = ApiErrorParser.parse(throwable, "Failed to load listing.")
            Result.failure(IllegalStateException(message))
        }
    }

    override suspend fun updateListing(
        listingPublicId: String,
        payload: DriverMarketplaceListingPayload,
    ): Result<DriverMarketplaceListing> {
        return try {
            Result.success(api.updateListing(listingPublicId, payload.toRequestDto()).data.toDomain())
        } catch (throwable: Throwable) {
            val message = ApiErrorParser.parse(throwable, "Failed to update listing.")
            Result.failure(IllegalStateException(message))
        }
    }
}
