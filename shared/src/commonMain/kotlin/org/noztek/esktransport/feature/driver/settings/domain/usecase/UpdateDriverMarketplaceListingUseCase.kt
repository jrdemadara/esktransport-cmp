package org.noztek.esktransport.feature.driver.settings.domain.usecase

import org.noztek.esktransport.feature.driver.settings.domain.model.DriverMarketplaceListing
import org.noztek.esktransport.feature.driver.settings.domain.model.DriverMarketplaceListingPayload
import org.noztek.esktransport.feature.driver.settings.domain.repository.DriverMarketplaceListingRepository

class UpdateDriverMarketplaceListingUseCase(
    private val repository: DriverMarketplaceListingRepository,
) {
    suspend operator fun invoke(
        listingPublicId: String,
        payload: DriverMarketplaceListingPayload,
    ): Result<DriverMarketplaceListing> {
        return repository.updateListing(listingPublicId, payload)
    }
}
