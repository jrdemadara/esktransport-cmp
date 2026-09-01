package org.noztek.esktransport.feature.common.marketplace_chat.domain.repository

import org.noztek.esktransport.feature.common.marketplace_chat.domain.model.MarketplaceChatMessage
import org.noztek.esktransport.feature.common.marketplace_chat.domain.model.MarketplaceChatThread
import org.noztek.esktransport.feature.common.marketplace_chat.domain.model.MarketplaceConversation
import org.noztek.esktransport.feature.common.marketplace_chat.domain.model.SendOwnerMarketplaceChatMessagePayload
import org.noztek.esktransport.feature.common.marketplace_chat.domain.model.SendMarketplaceChatMessagePayload

interface MarketplaceChatRepository {
    suspend fun openPassengerConversationByListing(listingPublicId: String): Result<MarketplaceConversation>
    suspend fun getPassengerMessagesByListing(listingPublicId: String): Result<MarketplaceChatThread>
    suspend fun sendPassengerMessageByListing(payload: SendMarketplaceChatMessagePayload): Result<MarketplaceChatMessage>
    suspend fun getOwnerMessagesByThread(threadId: Long): Result<MarketplaceChatThread>
    suspend fun sendOwnerMessageByThread(payload: SendOwnerMarketplaceChatMessagePayload): Result<MarketplaceChatMessage>
}
