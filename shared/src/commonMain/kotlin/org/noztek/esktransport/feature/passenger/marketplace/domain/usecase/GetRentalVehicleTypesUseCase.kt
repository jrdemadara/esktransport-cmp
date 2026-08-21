package org.noztek.esktransport.feature.passenger.marketplace.domain.usecase

import org.noztek.esktransport.feature.passenger.marketplace.domain.model.MarketplaceVehicleType
import org.noztek.esktransport.feature.passenger.marketplace.domain.repository.MarketplaceRepository

class GetRentalVehicleTypesUseCase(
    private val repository: MarketplaceRepository,
) {
    suspend operator fun invoke(): Result<List<MarketplaceVehicleType>> {
        return repository.getRentalVehicleTypes()
    }
}
