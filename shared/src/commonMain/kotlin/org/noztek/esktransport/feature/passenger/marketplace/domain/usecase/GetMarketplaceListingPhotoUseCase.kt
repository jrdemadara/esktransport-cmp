package org.noztek.esktransport.feature.passenger.marketplace.domain.usecase

import org.noztek.esktransport.feature.passenger.marketplace.domain.repository.MarketplaceRepository

class GetMarketplaceListingPhotoUseCase(
    private val repository: MarketplaceRepository,
) {
    suspend operator fun invoke(publicId: String): Result<ByteArray?> {
        return repository.getListingPhoto(publicId)
    }
}
