package org.noztek.esktransport.feature.driver.settings.domain.repository

import org.noztek.esktransport.feature.driver.onboarding.domain.model.DriverVehicleServiceType
import org.noztek.esktransport.feature.driver.settings.domain.model.DriverMarketplaceListing
import org.noztek.esktransport.feature.driver.settings.domain.model.DriverMarketplaceListingPayload

interface DriverMarketplaceListingRepository {
    suspend fun getListingForVehicle(
        vehiclePublicId: String,
        serviceType: DriverVehicleServiceType,
    ): Result<DriverMarketplaceListing>

    suspend fun updateListing(
        listingPublicId: String,
        payload: DriverMarketplaceListingPayload,
    ): Result<DriverMarketplaceListing>
}
