package org.noztek.esktransport.feature.passenger.marketplace.domain.usecase

import org.noztek.esktransport.feature.passenger.marketplace.domain.model.MarketplaceListing
import org.noztek.esktransport.feature.passenger.marketplace.domain.repository.MarketplaceRepository

class GetMarketplaceListingUseCase(
    private val repository: MarketplaceRepository,
) {
    suspend operator fun invoke(publicId: String): Result<MarketplaceListing> {
        return repository.getListing(publicId)
    }
}
