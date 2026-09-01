package org.noztek.esktransport.feature.common.marketplace_chat.domain.usecase

import org.noztek.esktransport.feature.common.marketplace_chat.domain.model.MarketplaceChatMessage
import org.noztek.esktransport.feature.common.marketplace_chat.domain.model.SendOwnerMarketplaceChatMessagePayload
import org.noztek.esktransport.feature.common.marketplace_chat.domain.repository.MarketplaceChatRepository

class SendOwnerMarketplaceMessageUseCase(
    private val repository: MarketplaceChatRepository,
) {
    suspend operator fun invoke(payload: SendOwnerMarketplaceChatMessagePayload): Result<MarketplaceChatMessage> {
        return repository.sendOwnerMessageByThread(payload)
    }
}
