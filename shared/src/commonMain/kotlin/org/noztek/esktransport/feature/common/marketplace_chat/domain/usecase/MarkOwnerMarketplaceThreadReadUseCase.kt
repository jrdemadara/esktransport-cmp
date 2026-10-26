package org.noztek.esktransport.feature.common.marketplace_chat.domain.usecase

import org.noztek.esktransport.feature.common.marketplace_chat.domain.repository.MarketplaceChatRepository

class MarkOwnerMarketplaceThreadReadUseCase(
    private val repository: MarketplaceChatRepository,
) {
    suspend operator fun invoke(threadId: Long) = repository.markOwnerThreadRead(threadId)
}
