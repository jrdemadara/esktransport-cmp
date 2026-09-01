package org.noztek.esktransport.feature.common.marketplace_chat.domain.usecase

import org.noztek.esktransport.feature.common.marketplace_chat.domain.model.MarketplaceChatMessage
import org.noztek.esktransport.feature.common.marketplace_chat.domain.model.SendMarketplaceChatMessagePayload
import org.noztek.esktransport.feature.common.marketplace_chat.domain.repository.MarketplaceChatRepository

class SendPassengerMarketplaceMessageUseCase(
    private val repository: MarketplaceChatRepository,
) {
    suspend operator fun invoke(payload: SendMarketplaceChatMessagePayload): Result<MarketplaceChatMessage> {
        return repository.sendPassengerMessageByListing(payload)
    }
}
