package org.noztek.esktransport.feature.common.marketplace_chat.domain.usecase

import org.noztek.esktransport.feature.common.marketplace_chat.domain.model.MarketplaceChatThread
import org.noztek.esktransport.feature.common.marketplace_chat.domain.repository.MarketplaceChatRepository

class GetPassengerMarketplaceMessagesUseCase(
    private val repository: MarketplaceChatRepository,
) {
    suspend operator fun invoke(listingPublicId: String): Result<MarketplaceChatThread> {
        return repository.getPassengerMessagesByListing(listingPublicId)
    }
}
