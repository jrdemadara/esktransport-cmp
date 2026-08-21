package org.noztek.esktransport.feature.passenger.marketplace.domain.usecase

import org.noztek.esktransport.feature.passenger.marketplace.domain.model.MarketplaceListing
import org.noztek.esktransport.feature.passenger.marketplace.domain.repository.MarketplaceRepository

class GetRentalListingsUseCase(
    private val repository: MarketplaceRepository,
) {
    suspend operator fun invoke(vehicleTypeCode: String?): Result<List<MarketplaceListing>> {
        return repository.getRentalListings(vehicleTypeCode)
    }
}
