package org.noztek.esktransport.feature.passenger.marketplace.domain.usecase

import org.noztek.esktransport.feature.passenger.marketplace.domain.model.CreateMarketplaceServiceRequestPayload
import org.noztek.esktransport.feature.passenger.marketplace.domain.repository.MarketplaceRepository

class SendMarketplaceVehicleRequestUseCase(
    private val repository: MarketplaceRepository,
) {
    suspend operator fun invoke(payload: CreateMarketplaceServiceRequestPayload) = repository.sendRequest(payload)
}
