package org.noztek.esktransport.feature.common.marketplace_chat.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MarketplaceConversationResponseDto(
    val message: String? = null,
    val data: MarketplaceConversationDto,
)

@Serializable
data class MarketplaceConversationDto(
    @SerialName("thread_id")
    val threadId: Long,
    @SerialName("listing_public_id")
    val listingPublicId: String,
    val listing: MarketplaceChatListingDto,
    val messages: List<MarketplaceChatMessageDto> = emptyList(),
)

@Serializable
data class MarketplaceChatMessagesResponseDto(
    @SerialName("thread_id")
    val threadId: Long? = null,
    @SerialName("listing_public_id")
    val listingPublicId: String? = null,
    val data: List<MarketplaceChatMessageDto> = emptyList(),
)

@Serializable
data class MarketplaceChatMessageResponseDto(
    val message: String? = null,
    @SerialName("thread_id")
    val threadId: Long? = null,
    @SerialName("listing_public_id")
    val listingPublicId: String? = null,
    val data: MarketplaceChatMessageDto,
)

@Serializable
data class MarketplaceChatListingDto(
    @SerialName("public_id")
    val publicId: String,
    @SerialName("service_type")
    val serviceType: String,
    val title: String,
    val owner: MarketplaceChatOwnerDto,
)

@Serializable
data class MarketplaceChatOwnerDto(
    val name: String,
)

@Serializable
data class MarketplaceChatMessageDto(
    val id: Long,
    @SerialName("thread_id")
    val threadId: Long? = null,
    @SerialName("sender_user_id")
    val senderUserId: Long,
    @SerialName("sender_name")
    val senderName: String,
    val message: String,
    @SerialName("sent_at")
    val sentAt: String? = null,
    @SerialName("is_mine")
    val isMine: Boolean = false,
)

@Serializable
data class SendMarketplaceChatMessageRequestDto(
    val message: String,
)
