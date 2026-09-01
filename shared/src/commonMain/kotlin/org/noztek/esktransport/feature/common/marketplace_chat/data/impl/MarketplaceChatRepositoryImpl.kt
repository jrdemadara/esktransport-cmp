package org.noztek.esktransport.feature.common.marketplace_chat.data.impl

import org.noztek.esktransport.core.network.ApiErrorParser
import org.noztek.esktransport.feature.common.marketplace_chat.data.remote.MarketplaceChatApi
import org.noztek.esktransport.feature.common.marketplace_chat.data.remote.dto.SendMarketplaceChatMessageRequestDto
import org.noztek.esktransport.feature.common.marketplace_chat.domain.model.MarketplaceChatMessage
import org.noztek.esktransport.feature.common.marketplace_chat.domain.model.MarketplaceChatThread
import org.noztek.esktransport.feature.common.marketplace_chat.domain.model.MarketplaceConversation
import org.noztek.esktransport.feature.common.marketplace_chat.domain.model.SendOwnerMarketplaceChatMessagePayload
import org.noztek.esktransport.feature.common.marketplace_chat.domain.model.SendMarketplaceChatMessagePayload
import org.noztek.esktransport.feature.common.marketplace_chat.domain.repository.MarketplaceChatRepository

class MarketplaceChatRepositoryImpl(
    private val api: MarketplaceChatApi,
) : MarketplaceChatRepository {
    override suspend fun openPassengerConversationByListing(
        listingPublicId: String,
    ): Result<MarketplaceConversation> {
        return try {
            Result.success(api.openPassengerConversationByListing(listingPublicId).data.toDomain())
        } catch (throwable: Throwable) {
            val message = ApiErrorParser.parse(throwable, "Failed to open conversation.")
            Result.failure(IllegalStateException(message))
        }
    }

    override suspend fun getPassengerMessagesByListing(
        listingPublicId: String,
    ): Result<MarketplaceChatThread> {
        return try {
            val response = api.getPassengerMessagesByListing(listingPublicId)
            Result.success(
                MarketplaceChatThread(
                    threadId = response.threadId,
                    listingPublicId = response.listingPublicId,
                    messages = response.data.map { it.toDomain(response.listingPublicId) },
                ),
            )
        } catch (throwable: Throwable) {
            val message = ApiErrorParser.parse(throwable, "Failed to load marketplace chat.")
            Result.failure(IllegalStateException(message))
        }
    }

    override suspend fun sendPassengerMessageByListing(
        payload: SendMarketplaceChatMessagePayload,
    ): Result<MarketplaceChatMessage> {
        return try {
            val response = api.sendPassengerMessageByListing(
                listingPublicId = payload.listingPublicId,
                request = SendMarketplaceChatMessageRequestDto(message = payload.message),
            )
            Result.success(response.data.toDomain(response.listingPublicId))
        } catch (throwable: Throwable) {
            val message = ApiErrorParser.parse(throwable, "Failed to send message.")
            Result.failure(IllegalStateException(message))
        }
    }

    override suspend fun getOwnerMessagesByThread(threadId: Long): Result<MarketplaceChatThread> {
        return try {
            val response = api.getOwnerMessagesByThread(threadId)
            Result.success(
                MarketplaceChatThread(
                    threadId = response.threadId ?: threadId,
                    listingPublicId = response.listingPublicId,
                    messages = response.data.map { it.toDomain(response.listingPublicId) },
                ),
            )
        } catch (throwable: Throwable) {
            val message = ApiErrorParser.parse(throwable, "Failed to load marketplace chat.")
            Result.failure(IllegalStateException(message))
        }
    }

    override suspend fun sendOwnerMessageByThread(
        payload: SendOwnerMarketplaceChatMessagePayload,
    ): Result<MarketplaceChatMessage> {
        return try {
            val response = api.sendOwnerMessageByThread(
                threadId = payload.threadId,
                request = SendMarketplaceChatMessageRequestDto(message = payload.message),
            )
            Result.success(response.data.toDomain(response.listingPublicId))
        } catch (throwable: Throwable) {
            val message = ApiErrorParser.parse(throwable, "Failed to send message.")
            Result.failure(IllegalStateException(message))
        }
    }
}
