package org.noztek.esktransport.feature.common.marketplace_chat.domain.model

data class MarketplaceConversation(
    val threadId: Long,
    val listingPublicId: String,
    val listing: MarketplaceChatListing,
    val messages: List<MarketplaceChatMessage>,
)

data class MarketplaceChatThread(
    val threadId: Long?,
    val listingPublicId: String?,
    val messages: List<MarketplaceChatMessage>,
)

data class MarketplaceChatListing(
    val publicId: String,
    val serviceType: String,
    val title: String,
    val ownerName: String,
)

data class MarketplaceChatMessage(
    val id: Long,
    val threadId: Long?,
    val listingPublicId: String?,
    val senderUserId: Long,
    val senderName: String,
    val message: String,
    val sentAt: String?,
    val isMine: Boolean,
)

data class SendMarketplaceChatMessagePayload(
    val listingPublicId: String,
    val message: String,
)

data class SendOwnerMarketplaceChatMessagePayload(
    val threadId: Long,
    val message: String,
)

enum class MarketplaceChatParticipantRole {
    Passenger,
    Owner,
}
