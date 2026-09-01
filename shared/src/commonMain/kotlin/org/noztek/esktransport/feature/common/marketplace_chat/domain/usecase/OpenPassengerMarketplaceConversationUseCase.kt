package org.noztek.esktransport.feature.common.marketplace_chat.domain.usecase

import org.noztek.esktransport.feature.common.marketplace_chat.domain.model.MarketplaceConversation
import org.noztek.esktransport.feature.common.marketplace_chat.domain.repository.MarketplaceChatRepository

class OpenPassengerMarketplaceConversationUseCase(
    private val repository: MarketplaceChatRepository,
) {
    suspend operator fun invoke(listingPublicId: String): Result<MarketplaceConversation> {
        return repository.openPassengerConversationByListing(listingPublicId)
    }
}
