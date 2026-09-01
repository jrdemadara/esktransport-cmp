package org.noztek.esktransport.feature.common.marketplace_chat.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import org.noztek.esktransport.feature.common.marketplace_chat.data.remote.dto.MarketplaceChatMessageResponseDto
import org.noztek.esktransport.feature.common.marketplace_chat.data.remote.dto.MarketplaceChatMessagesResponseDto
import org.noztek.esktransport.feature.common.marketplace_chat.data.remote.dto.MarketplaceConversationResponseDto
import org.noztek.esktransport.feature.common.marketplace_chat.data.remote.dto.SendMarketplaceChatMessageRequestDto

class MarketplaceChatApi(
    private val client: HttpClient,
    private val baseUrl: String,
) {
    suspend fun openPassengerConversationByListing(
        listingPublicId: String,
    ): MarketplaceConversationResponseDto {
        return client.post("${baseUrl.trimEnd('/')}/api/passenger/vehicle-listings/$listingPublicId/conversation").body()
    }

    suspend fun getPassengerMessagesByListing(
        listingPublicId: String,
    ): MarketplaceChatMessagesResponseDto {
        return client.get("${baseUrl.trimEnd('/')}/api/passenger/vehicle-listings/$listingPublicId/messages").body()
    }

    suspend fun sendPassengerMessageByListing(
        listingPublicId: String,
        request: SendMarketplaceChatMessageRequestDto,
    ): MarketplaceChatMessageResponseDto {
        return client.post("${baseUrl.trimEnd('/')}/api/passenger/vehicle-listings/$listingPublicId/messages") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun getOwnerMessagesByThread(
        threadId: Long,
    ): MarketplaceChatMessagesResponseDto {
        return client.get("${baseUrl.trimEnd('/')}/api/v1/rider/vehicle-inquiries/$threadId/messages").body()
    }

    suspend fun sendOwnerMessageByThread(
        threadId: Long,
        request: SendMarketplaceChatMessageRequestDto,
    ): MarketplaceChatMessageResponseDto {
        return client.post("${baseUrl.trimEnd('/')}/api/v1/rider/vehicle-inquiries/$threadId/messages") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }
}
