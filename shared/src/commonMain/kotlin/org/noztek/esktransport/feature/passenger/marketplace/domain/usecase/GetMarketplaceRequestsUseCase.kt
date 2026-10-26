package org.noztek.esktransport.feature.passenger.marketplace.domain.usecase

import org.noztek.esktransport.feature.passenger.marketplace.domain.model.MarketplaceServiceRequest
import org.noztek.esktransport.feature.passenger.marketplace.domain.repository.MarketplaceRepository

class GetMarketplaceRequestsUseCase(
    private val repository: MarketplaceRepository,
) {
    suspend operator fun invoke(): Result<List<MarketplaceServiceRequest>> = repository.getRequests()
}
