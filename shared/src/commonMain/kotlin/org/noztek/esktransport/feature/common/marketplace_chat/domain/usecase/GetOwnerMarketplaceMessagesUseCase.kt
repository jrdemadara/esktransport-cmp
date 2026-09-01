package org.noztek.esktransport.feature.common.marketplace_chat.domain.usecase

import org.noztek.esktransport.feature.common.marketplace_chat.domain.model.MarketplaceChatThread
import org.noztek.esktransport.feature.common.marketplace_chat.domain.repository.MarketplaceChatRepository

class GetOwnerMarketplaceMessagesUseCase(
    private val repository: MarketplaceChatRepository,
) {
    suspend operator fun invoke(threadId: Long): Result<MarketplaceChatThread> {
        return repository.getOwnerMessagesByThread(threadId)
    }
}
